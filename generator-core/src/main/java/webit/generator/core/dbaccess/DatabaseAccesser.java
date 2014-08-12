// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.dbaccess;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import webit.generator.core.Config;
import webit.generator.core.dbaccess.model.ColumnRaw;
import webit.generator.core.dbaccess.model.ForeignKey;
import webit.generator.core.dbaccess.model.TableRaw;
import webit.generator.core.util.DBUtil;
import webit.generator.core.util.Logger;
import webit.generator.core.util.StringUtil;

public class DatabaseAccesser {

    private static final String PKTABLE_NAME = "PKTABLE_NAME";
    private static final String PKCOLUMN_NAME = "PKCOLUMN_NAME";
    private static final String FKTABLE_NAME = "FKTABLE_NAME";
    private static final String FKCOLUMN_NAME = "FKCOLUMN_NAME";
    private static final String KEY_SEQ = "KEY_SEQ";

    private static Connection connection;

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                final String driver = Config.getRequiredString("db.driver");
                try {
                    Class.forName(driver);
                    connection = DriverManager.getConnection(Config.getRequiredString("db.url"), Config.getRequiredString("db.username"), Config.getString("db.password"));
                    if (Logger.isDebugEnabled()) {
                        Logger.debug("Initialised connection: " + connection.getClass().getName());
                        Logger.debug("  Catalog: " + Config.getString("db.catalog"));
                        Logger.debug("  Schema: " + Config.getString("db.schema"));
                        Logger.debug("  ClientInfo: ");
                        Properties clientInfo = connection.getClientInfo();
                        if (clientInfo != null) {
                            for (Map.Entry<Object, Object> entry : clientInfo.entrySet()) {
                                Logger.debug("    " + entry.getKey() + '=' + entry.getValue());
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Not found jdbc driver class: [" + driver + "]", e);
                }
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static DatabaseAccesser instance;
    private TableCache tableCache;
    private ColumnCache columnCache;

    private Pattern includes;
    private Pattern excludes;

    final String catalog;
    final String schema;
    final boolean skipUnreadableTable;

    public synchronized static DatabaseAccesser getInstance() {
        if (instance == null) {
            instance = new DatabaseAccesser();
        }
        return instance;
    }

    private DatabaseAccesser() {
        this.skipUnreadableTable = Config.getBoolean("skipUnreadableTable", false);
        this.schema = Config.getString("db.schema");
        this.catalog = Config.getString("db.catalog");
        String includesString = Config.getString("includeTables");
        if (StringUtil.notEmpty(includesString)) {
            this.includes = Pattern.compile(includesString);
        }
        String excludesString = Config.getString("excludeTables");
        if (StringUtil.notEmpty(excludesString)) {
            this.excludes = Pattern.compile(excludesString);
        }
    }

    private boolean isTableInclude(final String tableName) {
        return (includes == null || includes.matcher(tableName).matches())
                && (excludes == null || !excludes.matcher(tableName).matches());
    }

    public synchronized Map<String, TableRaw> getAllTables() {
        if (this.tableCache != null) {
            return tableCache.getTables();
        }
        tableCache = new TableCache();
        columnCache = new ColumnCache();
        try {
            final ResultSet rs;
            final Connection conn = getConnection();
            if (DBUtil.getDBType().equals("mysql")) { //FIXED: allways REMARKS==null in mysql

                PreparedStatement ps = conn.prepareStatement("SELECT TABLE_SCHEMA AS TABLE_CAT, "
                        + "NULL AS TABLE_SCHEM, TABLE_NAME, "
                        + "CASE WHEN TABLE_TYPE='BASE TABLE' THEN 'TABLE' WHEN TABLE_TYPE='TEMPORARY' THEN 'LOCAL_TEMPORARY' ELSE TABLE_TYPE END AS TABLE_TYPE, "
                        + "TABLE_COMMENT AS REMARKS "
                        + "FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?;");
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
                            Logger.debug("Skip table (by DatabaseAccesser): " + tableName);
                        }
                        continue;
                    }
                    if (Logger.isDebugEnabled()) {
                        Logger.debug("Found table " + tableName);
                    }
                    tableCache.put(new TableRaw(tableName, remark, isView));
                }
            } finally {
                rs.close();
            }

            final Map<String, TableRaw> tables = tableCache.getTables();
            for (Iterator<Map.Entry<String, TableRaw>> it = tables.entrySet().iterator(); it.hasNext();) {
                TableRaw table = it.next().getValue();
                boolean result = resolveTableColumns(table);
                if (result == false) {
                    if (skipUnreadableTable) {
                        Logger.warn("SKIP TABLE, unreadable: " + table);
                        it.remove();
                    } else {
                        throw new RuntimeException("CAN't SKIP TABLE: " + table);
                    }
                }
            }
            for (Map.Entry<String, TableRaw> entry : tables.entrySet()) {
                resolveFKS(entry.getValue());
            }
            return tables;
        } catch (SQLException e) {
            Logger.error("Unable to read table info.", e);
            throw new RuntimeException("Unable to read table info.", e);
        }
    }

    private DatabaseMetaData getMetaData() throws SQLException {
        return getConnection().getMetaData();
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
                String pktable = fkeys.getString(PKTABLE_NAME);
                String pkcol = fkeys.getString(PKCOLUMN_NAME);
                String fktable = fkeys.getString(FKTABLE_NAME);
                String fkcol = fkeys.getString(FKCOLUMN_NAME);
                Integer iseq = Integer.valueOf(fkeys.getString(KEY_SEQ));

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

    protected static class ColumnCache {

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

    protected static class TableCache {

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

        public Map<String, TableRaw> getTables() {
            return tables;
        }
    }
}
