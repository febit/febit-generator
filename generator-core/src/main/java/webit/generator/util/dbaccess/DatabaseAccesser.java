// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.util.dbaccess;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import webit.generator.Config;
import webit.generator.util.Logger;
import webit.generator.util.StringUtil;
import webit.generator.util.dbaccess.model.ColumnRaw;
import webit.generator.util.dbaccess.model.ForeignKey;
import webit.generator.util.dbaccess.model.TableRaw;

public class DatabaseAccesser {

    private static DatabaseAccesser instance;
    private static Connection connection;

    private TableCache tableCache;
    private ColumnCache columnCache;

    private Pattern includes;
    private Pattern excludes;

    private final boolean skipUnreadableTable;
    private final String catalog;
    private final String schema;

    private DatabaseAccesser() {
        this.skipUnreadableTable = Config.getBoolean("skipUnreadableTable", false);
        this.catalog = Config.getString("db.catalog");
        this.schema = Config.getString("db.schema");
        String includesString = Config.getString("includeTables");
        String excludesString = Config.getString("excludeTables");
        if (StringUtil.notEmpty(includesString)) {
            this.includes = Pattern.compile(includesString);
        }
        if (StringUtil.notEmpty(excludesString)) {
            this.excludes = Pattern.compile(excludesString);
        }
    }

    private boolean isTableInclude(final String tableName) {
        return (includes == null || includes.matcher(tableName).matches())
                && (excludes == null || !excludes.matcher(tableName).matches());
    }

    public Collection<TableRaw> getAllTables() {
        TableCache myTableCache = this.tableCache;
        if (myTableCache != null) {
            return tableCache.getTables();
        }
        myTableCache = new TableCache();
        columnCache = new ColumnCache();
        final String jdbcType = getJdbcType();
        try {
            final ResultSet rs;
            if ("mysql".equals(jdbcType)) {
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT TABLE_SCHEMA AS TABLE_CAT, NULL AS TABLE_SCHEM, TABLE_NAME, CASE WHEN TABLE_TYPE='BASE TABLE' THEN 'TABLE' WHEN TABLE_TYPE='TEMPORARY' THEN 'LOCAL_TEMPORARY' ELSE TABLE_TYPE END AS TABLE_TYPE, TABLE_COMMENT AS REMARKS FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?;");
                ps.setString(1, conn.getCatalog());
                rs = ps.executeQuery();
            } else {
                rs = getMetaData().getTables(catalog, schema, null, null);
            }
            try {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String remark = rs.getString("REMARKS");
                    boolean isView = "VIEW".equalsIgnoreCase(rs.getString("TABLE_TYPE"));
                    if (!isTableInclude(tableName)) {
                        if (Logger.isDebugEnabled()) {
                            Logger.debug("Skip table (by DatabaseAccesser): " + tableName + (remark != null ? "  " + remark : ""));
                        }
                        continue;
                    }
                    if (Logger.isDebugEnabled()) {
                        Logger.debug("Found table " + tableName + (remark != null ? "  " + remark : ""));
                    }
                    myTableCache.put(new TableRaw(tableName, remark, isView));
                }
            } finally {
                rs.close();
            }

            for (Iterator<TableRaw> it = myTableCache.getTables().iterator(); it.hasNext();) {
                TableRaw table = it.next();
                boolean result = resolveTableColumns(table);
                if (!result) {
                    if (skipUnreadableTable) {
                        Logger.warn("Skip table, unreadable: " + table);
                        it.remove();
                    } else {
                        throw new RuntimeException("Can't skip table: " + table);
                    }
                }
            }
            for (TableRaw table : myTableCache.getTables()) {
                resolveFKS(table);
            }
            return myTableCache.getTables();
        } catch (SQLException e) {
            Logger.error("Unable to read table info.", e);
            throw new RuntimeException("Unable to read table info.", e);
        }
    }

    private boolean resolveTableColumns(TableRaw table) throws SQLException {
        if (Logger.isTraceEnabled()) {
            Logger.trace("Scanf table's columns: " + table);
        }

        List<String> primaryKeys = getTablePrimaryKeys(table);
        if (primaryKeys.isEmpty()) {
            Logger.warn("Not found primary key columns in table: " + table);
        }

        final List<String> indices = new ArrayList<String>();
        final Map<String, String> uniqueIndices = new HashMap<String, String>();
        final Map<String, List<String>> uniqueColumns = new HashMap<String, List<String>>();
        final ResultSet indexRs;
        try {
            indexRs = getMetaData().getIndexInfo(catalog, schema, table.name, false, true);
        } catch (SQLException ex) {
            Logger.error("Unable to getIndexInfo of table: " + table);
            return false;
        }
        try {
            while (indexRs.next()) {
                String columnName = indexRs.getString("COLUMN_NAME");
                if (columnName != null) {
                    //Logger.trace("Indexed column:" + columnName);
                    indices.add(columnName);
                    String indexName = indexRs.getString("INDEX_NAME");
                    boolean unique = !indexRs.getBoolean("NON_UNIQUE");
                    if (unique && indexName != null) {
                        List<String> list;
                        if ((list = uniqueColumns.get(indexName)) == null) {
                            uniqueColumns.put(indexName, list = new ArrayList<String>());
                        }
                        list.add(columnName);
                        uniqueIndices.put(columnName, indexName);
                        //Logger.trace("unique:" + columnName + " (" + indexName + ")");
                    }
                }
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            indexRs.close();
        }

        final List<ColumnRaw> columns = getTableColumns(table, primaryKeys, indices, uniqueIndices, uniqueColumns);
        table.addColumns(columns);
        columnCache.addColums(columns);
        return true;
    }

    private List<ColumnRaw> getTableColumns(TableRaw table, List<String> primaryKeys, List<String> indices, Map<String, String> uniqueIndices, Map<String, List<String>> uniqueColumns) throws SQLException {

        final List<ColumnRaw> columns = new ArrayList<ColumnRaw>();
        final ResultSet columnRs = getMetaData().getColumns(catalog, schema, table.name, null);

        try {
            while (columnRs.next()) {
                int sqlType = columnRs.getInt("DATA_TYPE");
                String sqlTypeName = columnRs.getString("TYPE_NAME");
                String columnName = columnRs.getString("COLUMN_NAME");
                String columnDefaultValue = columnRs.getString("COLUMN_DEF");

                String remarks = columnRs.getString("REMARKS");

                boolean isNullable = (DatabaseMetaData.columnNullable == columnRs.getInt("NULLABLE"));
                int size = columnRs.getInt("COLUMN_SIZE");
                int decimalDigits = columnRs.getInt("DECIMAL_DIGITS");

                boolean isPk = primaryKeys.contains(columnName);
                boolean isIndexed = indices.contains(columnName);
                String uniqueIndex = uniqueIndices.get(columnName);
                List columnsInUniqueIndex = null;
                if (uniqueIndex != null) {
                    columnsInUniqueIndex = uniqueColumns.get(uniqueIndex);
                }
                boolean isUnique = columnsInUniqueIndex != null && columnsInUniqueIndex.size() == 1;

                ColumnRaw column = new ColumnRaw(
                        table,
                        sqlType,
                        sqlTypeName,
                        columnName,
                        size,
                        decimalDigits,
                        isPk,
                        isNullable,
                        isIndexed,
                        isUnique,
                        columnDefaultValue,
                        remarks);
                columns.add(column);
            }
        } finally {
            columnRs.close();
        }
        return columns;
    }

    private void resolveFKS(TableRaw table) throws SQLException {
        final ResultSet fkeys = getMetaData().getImportedKeys(catalog, schema, table.name);
        try {
            while (fkeys.next()) {
                String pktable = fkeys.getString("PKTABLE_NAME");
                String pkcol = fkeys.getString("PKCOLUMN_NAME");
                String fktable = fkeys.getString("FKTABLE_NAME");
                String fkcol = fkeys.getString("FKCOLUMN_NAME");
                Integer iseq = Integer.valueOf(fkeys.getString("KEY_SEQ"));

                ColumnRaw pkColumn = columnCache.get(pktable, pkcol);
                ColumnRaw fkColumn = columnCache.get(fktable, fkcol);
                if (pkColumn == null) {
                    Logger.warn("Foreign linked column not found: " + pktable + '.' + pkcol);
                    continue;
                }
                if (fkColumn == null) {
                    Logger.warn("Foreign column not found: " + fktable + '.' + fkcol);
                    continue;
                }
                ForeignKey fk = new ForeignKey(pkColumn, fkColumn, iseq);
                fkColumn.setIsFk(true);
                fkColumn.setHasOne(fk);
                tableCache.get(pktable).addExportedKeys(fk);
                tableCache.get(fktable).addImportedKeys(fk);
            }
        } finally {
            fkeys.close();
        }
    }

    private List<String> getTablePrimaryKeys(TableRaw table) throws SQLException {
        final List<String> primaryKeys = new ArrayList<String>();
        final ResultSet primaryKeyRs = getMetaData().getPrimaryKeys(catalog, schema, table.name);
        try {
            while (primaryKeyRs.next()) {
                primaryKeys.add(primaryKeyRs.getString("COLUMN_NAME"));
            }
        } finally {
            primaryKeyRs.close();
        }
        return primaryKeys;
    }

    public static DatabaseAccesser getInstance() {
        DatabaseAccesser accesser = instance;
        if (accesser == null) {
            accesser = instance = new DatabaseAccesser();
        }
        return accesser;
    }

    private static DatabaseMetaData getMetaData() throws SQLException {
        return getConnection().getMetaData();
    }

    public static Connection getConnection() {
        try {
            Connection conn = connection;
            if (conn == null || conn.isClosed()) {
                final String driver = Config.getRequiredString("db.driver");
                try {
                    Class.forName(driver);

                    String url = Config.getRequiredString("db.url");
                    String user = Config.getString("db.username");
                    String password = Config.getString("db.password");
                    final String JdbcType = getJdbcType(url);
                    Logger.debug("Jdbc Type: " + JdbcType);
                    Properties info = new Properties();
                    if (user != null) {
                        info.put("user", user);
                    }
                    if (password != null) {
                        info.put("password", password);
                    }
                    if ("oracle".equals(JdbcType)) {
                        info.put("remarksReporting", "true");
                    }
                    conn = DriverManager.getConnection(url, info);
                    if (Logger.isDebugEnabled()) {
                        Logger.debug("Initialized connection: " + conn.getClass().getName());
                        Logger.debug("  Catalog: " + Config.getString("db.catalog"));
                        Logger.debug("  Schema: " + Config.getString("db.schema"));
                    }
                    connection = conn;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Not found jdbc driver class: [" + driver + "]", e);
                }
            }
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getJdbcType(String url) {
        if (url.startsWith("jdbc:")) {
            int endindex = url.indexOf(':', 5);
            if (endindex > 0) {
                return url.substring(5, endindex).toLowerCase();
            }
        }
        return "unkown";
    }

    public static String getJdbcType() {
        return getJdbcType(Config.getRequiredString("db.url"));
    }

    public static String getJdbcTypeString(int type) {
        switch (type) {
            case Types.BIT:
                return "BIT";
            case Types.TINYINT:
                return "TINYINT";
            case Types.SMALLINT:
                return "SMALLINT";
            case Types.INTEGER:
                return "INTEGER";
            case Types.BIGINT:
                return "BIGINT";
            case Types.FLOAT:
                return "FLOAT";
            case Types.REAL:
                return "REAL";
            case Types.DOUBLE:
                return "DOUBLE";
            case Types.NUMERIC:
                return "NUMERIC";
            case Types.DECIMAL:
                return "DECIMAL";
            case Types.CHAR:
                return "CHAR";
            case Types.VARCHAR:
                return "VARCHAR";
            case Types.LONGVARCHAR:
                return "LONGVARCHAR";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.BINARY:
                return "BINARY";
            case Types.VARBINARY:
                return "VARBINARY";
            case Types.LONGVARBINARY:
                return "LONGVARBINARY";
            case Types.NULL:
                return "NULL";
            case Types.OTHER:
                return "OTHER";
            case Types.JAVA_OBJECT:
                return "JAVA_OBJECT";
            case Types.DISTINCT:
                return "DISTINCT";
            case Types.STRUCT:
                return "STRUCT";
            case Types.ARRAY:
                return "ARRAY";
            case Types.BLOB:
                return "BLOB";
            case Types.CLOB:
                return "CLOB";
            case Types.REF:
                return "REF";
            case Types.DATALINK:
                return "DATALINK";
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.ROWID:
                return "ROWID";
            case Types.NCHAR:
                return "NCHAR";
            case Types.NVARCHAR:
                return "NVARCHAR";
            case Types.LONGNVARCHAR:
                return "LONGNVARCHAR";
            case Types.NCLOB:
                return "NCLOB";
            case Types.SQLXML:
                return "SQLXML";
            default:
                return null;
        }
    }

    public static String getJavaType(final int sqlType, final int size, final int decimalDigits) {
        switch (sqlType) {
            case Types.TINYINT:
                if (size == 1) {
                    return "java.lang.Boolean";
                }
                return "java.lang.Byte";
            case Types.SMALLINT:
                return "java.lang.Short";
            case Types.INTEGER:
                return "java.lang.Integer";
            case Types.BIGINT:
                return "java.lang.Long";
            case Types.REAL:
                return "java.lang.Float";
            case Types.FLOAT:
            case Types.DOUBLE:
                return "java.lang.Double";
            case Types.DECIMAL:
            case Types.NUMERIC:
                if (decimalDigits == 0) {
                    if (size == 1) {
                        return "java.lang.Boolean";
                    } else if (size <= 2) {
                        return "java.lang.Byte";
                    } else if (size <= 5) {
                        return "java.lang.Short";
                    } else if (size <= 11) {
                        return "java.lang.Integer";
                    } else if (size <= 20) {
                        return "java.lang.Long";
                    } else {
                        return "java.math.BigInteger";
                    }
                }
                return "java.math.BigDecimal";
            case Types.BIT:
            case Types.BOOLEAN:
                return "java.lang.Boolean";
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NVARCHAR:
                return "java.lang.String";
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "byte[]";
            case Types.DATE:
                return "java.sql.Date";
            case Types.TIME:
                return "java.sql.Time";
            case Types.TIMESTAMP:
                return "java.sql.Timestamp";
            case Types.CLOB:
                return "java.sql.Clob";
            case Types.BLOB:
                return "java.sql.Blob";
            case Types.ARRAY:
                return "java.sql.Array";
            case Types.REF:
                return "java.sql.Ref";
            case Types.STRUCT:
            case Types.JAVA_OBJECT:
            default:
                return "java.lang.Object";
        }
    }

    private static class ColumnCache {

        private final Map<String, ColumnRaw> columns = new HashMap<String, ColumnRaw>();

        private String getHashKey(String tableName, String columName) {
            return new StringBuilder(tableName).append('.').append(columName).toString();
        }

        private String getHashKey(ColumnRaw column) {
            return getHashKey(column.table.name, column.name);
        }

        public void addColums(Iterable<ColumnRaw> columns) {
            for (ColumnRaw column : columns) {
                put(column);
            }
        }

        public Map<String, ColumnRaw> getColumns() {
            return columns;
        }

        public void put(ColumnRaw column) {
            columns.put(getHashKey(column), column);
        }

        public ColumnRaw get(String tableName, String columName) {
            return columns.get(getHashKey(tableName, columName));
        }

        public boolean contains(String tableName, String columName) {
            return columns.containsKey(getHashKey(tableName, columName));
        }
    }

    private static class TableCache {

        private final Map<String, TableRaw> tables = new HashMap<String, TableRaw>();

        public void put(TableRaw table) {
            tables.put(table.name, table);
        }

        private String getHashKey(String tableName) {
            return tableName.toLowerCase();
        }

        public TableRaw get(String tableName) {

            return tables.get(getHashKey(tableName));
        }

        public boolean contains(String tableName) {
            return tables.containsKey(getHashKey(tableName));
        }

        public Collection<TableRaw> getTables() {
            return tables.values();
        }

        public Map<String, TableRaw> getTableMap() {
            return tables;
        }
    }
}
