/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.generator.util.dbaccess;

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
import org.febit.generator.util.Logger;

public class DatabaseAccesser {

    protected Pattern includeTables;
    protected Pattern excludeTables;
    protected Pattern includeColumns;
    protected Pattern excludeColumns;

    protected boolean skipUnreadableTable;
    protected String catalog;
    protected String schema;
    protected String driver;
    protected String url;
    protected String username;
    protected String password;

    private Connection connection;
    private final TableCache tableCache = new TableCache();
    private final ColumnCache columnCache = new ColumnCache();

    public DatabaseAccesser() {
    }

    protected boolean isColumnInclude(final String columnName) {
        return (includeColumns == null || includeColumns.matcher(columnName).matches())
                && (excludeColumns == null || !excludeColumns.matcher(columnName).matches());
    }

    protected boolean isTableInclude(final String tableName) {
        return (includeTables == null || includeTables.matcher(tableName).matches())
                && (excludeTables == null || !excludeTables.matcher(tableName).matches());
    }

    public synchronized Collection<TableRaw> getAllTables() {
        tableCache.clear();
        columnCache.clear();
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
                        Logger.debug("Skip table (by DatabaseAccesser): {} {}", tableName, remark);
                        continue;
                    }
                    Logger.debug("Found table {} {}", tableName, remark);
                    tableCache.put(new TableRaw(tableName, remark, isView));
                }
            } finally {
                rs.close();
            }

            for (Iterator<TableRaw> it = tableCache.getTables().iterator(); it.hasNext();) {
                TableRaw table = it.next();
                boolean result = resolveTableColumns(table);
                if (!result) {
                    if (skipUnreadableTable) {
                        Logger.warn("Skip table, unreadable: {}", table);
                        it.remove();
                    } else {
                        throw new RuntimeException("Can't skip table: " + table);
                    }
                }
            }
            for (TableRaw table : tableCache.getTables()) {
                resolveFKS(table);
            }
            return tableCache.getTables();
        } catch (SQLException e) {
            Logger.error("Unable to read table info.", e);
            throw new RuntimeException("Unable to read table info.", e);
        }
    }

    private boolean resolveTableColumns(TableRaw table) throws SQLException {
        Logger.trace("Scanf table's columns: {}", table);
        List<String> primaryKeys = getTablePrimaryKeys(table);
        if (primaryKeys.isEmpty()) {
            Logger.warn("Not found primary key columns in table: {}", table);
        }

        final List<String> indices = new ArrayList<>();
        final Map<String, String> uniqueIndices = new HashMap<>();
        final Map<String, List<String>> uniqueColumns = new HashMap<>();
        final ResultSet indexRs;
        try {
            indexRs = getMetaData().getIndexInfo(catalog, schema, table.name, false, true);
        } catch (SQLException ex) {
            Logger.error("Unable to getIndexInfo of table: {}", table);
            return false;
        }
        try {
            while (indexRs.next()) {
                String columnName = indexRs.getString("COLUMN_NAME");
                if (columnName == null) {
                    continue;
                }
                //Logger.trace("Indexed column:" + columnName);
                indices.add(columnName);
                String indexName = indexRs.getString("INDEX_NAME");
                boolean unique = !indexRs.getBoolean("NON_UNIQUE");
                if (unique && indexName != null) {
                    List<String> list;
                    if ((list = uniqueColumns.get(indexName)) == null) {
                        uniqueColumns.put(indexName, list = new ArrayList<>());
                    }
                    list.add(columnName);
                    uniqueIndices.put(columnName, indexName);
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
        final List<ColumnRaw> columns = new ArrayList<>();
        try (ResultSet columnRs = getMetaData().getColumns(catalog, schema, table.name, null)) {
            while (columnRs.next()) {
                int sqlType = columnRs.getInt("DATA_TYPE");
                String sqlTypeName = columnRs.getString("TYPE_NAME");
                String columnName = columnRs.getString("COLUMN_NAME");
                String columnDefaultValue = columnRs.getString("COLUMN_DEF");
                String remarks = columnRs.getString("REMARKS");
                if (!isColumnInclude(columnName)) {
                    Logger.debug("Skip column (by DatabaseAccesser): {}.{} {}", table.name, columnName, remarks);
                    continue;
                }
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
        }
        return columns;
    }

    private void resolveFKS(TableRaw table) throws SQLException {
        try (ResultSet fkeys = getMetaData().getImportedKeys(catalog, schema, table.name)) {
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
                TableRaw pktableRaw = tableCache.get(pktable);
                if (pktableRaw == null) {
                    Logger.error("Foreign pk table not found: " + pktable + ", for " + fktable + '.' + fkcol);
                    continue;
                }
                TableRaw fktableRaw = tableCache.get(fktable);
                if (fktableRaw == null) {
                    Logger.error("Foreign fk table not found: " + fktable + ", for " + fktable + '.' + fkcol);
                    continue;
                }
                pktableRaw.addExportedKeys(fk);
                fktableRaw.addImportedKeys(fk);
            }
        }
    }

    private List<String> getTablePrimaryKeys(TableRaw table) throws SQLException {
        final List<String> primaryKeys = new ArrayList<>();
        try (ResultSet primaryKeyRs = getMetaData().getPrimaryKeys(catalog, schema, table.name)) {
            while (primaryKeyRs.next()) {
                primaryKeys.add(primaryKeyRs.getString("COLUMN_NAME"));
            }
        }
        return primaryKeys;
    }

    private DatabaseMetaData getMetaData() throws SQLException {
        return getConnection().getMetaData();
    }

    public String getJdbcType() {
        return getJdbcType(url);
    }

    public Connection getConnection() {
        try {
            Connection conn = connection;
            if (conn == null || conn.isClosed()) {
                try {
                    Class.forName(driver);
                    final String JdbcType = getJdbcType(url);
                    Logger.debug("Jdbc Type: " + JdbcType);
                    Properties info = new Properties();
                    if (username != null) {
                        info.put("user", username);
                    }
                    if (password != null) {
                        info.put("password", password);
                    }
                    if ("oracle".equals(JdbcType)) {
                        info.put("remarksReporting", "true");
                    }
                    conn = DriverManager.getConnection(url, info);
                    Logger.debug("Initialized connection: {} ", conn.getClass().getName());
                    Logger.debug("  Catalog: {}", catalog);
                    Logger.debug("  Schema: {}", catalog);
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

        private final Map<String, ColumnRaw> columns = new HashMap<>();

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

        public void clear() {
            columns.clear();
        }
    }

    private static class TableCache {

        private final Map<String, TableRaw> tables = new HashMap<>();

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

        public void clear() {
            tables.clear();
        }
    }
}
