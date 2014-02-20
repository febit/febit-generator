// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.dbaccess;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webit.generator.core.dbaccess.model.Column;
import webit.generator.core.dbaccess.model.ForeignKey;
import webit.generator.core.dbaccess.model.Table;
import webit.generator.core.util.DBUtil;
import webit.generator.core.util.Logger;

public class DatabaseAccesser {

    private static final String PKTABLE_NAME = "PKTABLE_NAME";
    private static final String PKCOLUMN_NAME = "PKCOLUMN_NAME";
    private static final String FKTABLE_NAME = "FKTABLE_NAME";
    private static final String FKCOLUMN_NAME = "FKCOLUMN_NAME";
    private static final String KEY_SEQ = "KEY_SEQ";

    private static DatabaseAccesser instance;
    private TableCache tableCache;
    private ColumnCache columnCache;

    public synchronized static DatabaseAccesser getInstance() {
        if (instance == null) {
            instance = new DatabaseAccesser();
        }
        return instance;
    }

    public synchronized Map<String, Table> getAllTables() {
        if (this.tableCache != null) {
            return tableCache.getTables();
        }
        tableCache = new TableCache();
        columnCache = new ColumnCache();
        try {
            final ResultSet rs;
            if (DBUtil.getDBType().equals("mysql")) { //FIXED: allways REMARKS==null in mysql
                final Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT TABLE_SCHEMA AS TABLE_CAT, "
                        + "NULL AS TABLE_SCHEM, TABLE_NAME, "
                        + "CASE WHEN TABLE_TYPE='BASE TABLE' THEN 'TABLE' WHEN TABLE_TYPE='TEMPORARY' THEN 'LOCAL_TEMPORARY' ELSE TABLE_TYPE END AS TABLE_TYPE, "
                        + "TABLE_COMMENT AS REMARKS "
                        + "FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?;");
                ps.setString(1, conn.getCatalog());
                rs = ps.executeQuery();
            } else {
                rs = getMetaData().getTables(null, null, null, null);
            }
            try {
                while (rs.next()) {
                    tableCache.put(new Table(rs.getString("TABLE_NAME"), rs.getString("REMARKS")));
                }
            } finally {
                rs.close();
            }

            final Map<String, Table> tables = tableCache.getTables();
            for (Map.Entry<String, Table> entry : tables.entrySet()) {
                resolveTableColumns(entry.getValue());
            }
            for (Map.Entry<String, Table> entry : tables.entrySet()) {
                resolveFKS(entry.getValue());
            }
            return tables;
        } catch (SQLException e) {
            Logger.error("Unable to read table info.", e);
            throw new RuntimeException("Unable to read table info.", e);
        }
    }

    private DatabaseMetaData getMetaData() throws SQLException {
        return DBUtil.getConnection().getMetaData();
    }

    private void resolveTableColumns(Table table) throws SQLException {
        Logger.trace("-------SCANF_TABLE(" + table.name + ")");

        List<String> primaryKeys = getTablePrimaryKeys(table);
        if (primaryKeys.isEmpty()) {
            Logger.warn("Not found primary key columns in table: " + table.name);
        }

        final List<String> indices = new ArrayList<String>();
        final Map<String, String> uniqueIndices = new HashMap<String, String>();
        final Map<String, List<String>> uniqueColumns = new HashMap<String, List<String>>();
        final ResultSet indexRs;

        indexRs = getMetaData().getIndexInfo(null, null, table.name, false, true);

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
            Logger.error("MetaData.getIndexInfo() failed", ex);
            throw ex;
        } finally {
            indexRs.close();
        }

        final List<Column> columns = getTableColumns(table, primaryKeys, indices, uniqueIndices, uniqueColumns);
        table.addColumns(columns);
        columnCache.addColums(columns);
    }

    private List<Column> getTableColumns(Table table, List<String> primaryKeys, List<String> indices, Map<String, String> uniqueIndices, Map<String, List<String>> uniqueColumns) throws SQLException {

        final List<Column> columns = new ArrayList<Column>();
        final ResultSet columnRs = getMetaData().getColumns(null, null, table.name, null);

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

                Column column = new Column(
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

    private void resolveFKS(Table table) throws SQLException {
        final ResultSet fkeys = getMetaData().getImportedKeys(null, null, table.name);
        try {
            while (fkeys.next()) {
                String pktable = fkeys.getString(PKTABLE_NAME);
                String pkcol = fkeys.getString(PKCOLUMN_NAME);
                String fktable = fkeys.getString(FKTABLE_NAME);
                String fkcol = fkeys.getString(FKCOLUMN_NAME);
                Integer iseq = Integer.valueOf(fkeys.getString(KEY_SEQ));

                Column pkColumn = columnCache.get(pktable, pkcol);
                Column fkColumn = columnCache.get(fktable, fkcol);
                if (pkColumn == null) {
                    Logger.warn("PKColumn CAN'T FOUND:" + pktable + "." + pkcol);
                    continue;
                }
                if (fkColumn == null) {
                    Logger.warn("FKColumn CAN'T FOUND:" + fktable + "." + fkcol);
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

    private List<String> getTablePrimaryKeys(Table table) throws SQLException {
        final List<String> primaryKeys = new ArrayList<String>();
        final ResultSet primaryKeyRs = getMetaData().getPrimaryKeys(null, null, table.name);
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

        private final Map<String, Column> columns = new HashMap<String, Column>();

        private String getHashKey(String tableName, String columName) {
            return new StringBuilder(tableName).append('.').append(columName).toString();
        }

        private String getHashKey(Column column) {
            return getHashKey(column.table.name, column.name);
        }

        public void addColums(Iterable<Column> columns) {
            for (Column column : columns) {
                put(column);
            }
        }

        public Map<String, Column> getColumns() {
            return columns;
        }

        public void put(Column column) {
            columns.put(getHashKey(column), column);
        }

        public Column get(String tableName, String columName) {
            return columns.get(getHashKey(tableName, columName));
        }

        public boolean contains(String tableName, String columName) {
            return columns.containsKey(getHashKey(tableName, columName));
        }
    }

    protected static class TableCache {

        private final Map<String, Table> tables = new HashMap<String, Table>();

        public void put(Table table) {
            tables.put(table.name, table);
        }

        private String getHashKey(String tableName) {
            return tableName.toLowerCase();
        }

        public Table get(String tableName) {

            return tables.get(getHashKey(tableName));
        }

        public boolean contains(String tableName) {
            return tables.containsKey(getHashKey(tableName));
        }

        public Map<String, Table> getTables() {
            return tables;
        }
    }
}
