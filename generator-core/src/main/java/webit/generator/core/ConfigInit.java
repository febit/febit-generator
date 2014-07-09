// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webit.generator.core.components.ConfigInitProcesser;
import webit.generator.core.components.TableFactory;
import webit.generator.core.model.Column;
import webit.generator.core.model.Table;
import webit.generator.core.util.Arrays;
import webit.generator.core.util.Logger;
import webit.generator.core.util.Maps;
import webit.generator.core.util.ResourceUtil;
import webit.generator.core.util.StringUtil;

/**
 *
 * @author Zqq
 */
public class ConfigInit {

    private List<Table> tables;
    private Map<String, Map<String, Map<String, Object>>> tablesColumns;
    private Map<String, Map<String, Map<String, Object>>> tablesColumnsOld;

    public Map<String, Map<String, Object>> getOldColumnMap(String tableName) {
        return tablesColumnsOld != null ? tablesColumnsOld.get(tableName) : null;
    }

    public Map<String, Map<String, Object>> getOldColumnMap(Table table) {
        Map<String, Map<String, Object>> result = getOldColumnMap(table.entity);
        if (result == null) {
            result = getOldColumnMap(table.sqlName);
        }
        return result;
    }

    public Map<String, Object> getOldColumnSettings(Column column) {
        return getOldColumnSettings(column.table, column.varName, column.sqlName);
    }

    public Map<String, Object> getOldColumnSettings(Table table, String varName) {
        return getOldColumnSettings(table, varName, null);
    }

    public Map<String, Object> getOldColumnSettings(Table table, String varName, String sqlName) {
        Map<String, Map<String, Object>> oldColumnMaps = this.getOldColumnMap(table);
        if (oldColumnMaps == null) {
            return null;
        }
        Map<String, Object> result = oldColumnMaps.get(varName);
        if (result == null && sqlName != null) {
            result = oldColumnMaps.get(sqlName);
        }
        return result;
    }

    public Map<String, Map<String, Object>> getTableColumns(Table table) {
        Map<String, Map<String, Object>> columnMaps = tablesColumns.get(table.entity);
        if (columnMaps == null) {
            columnMaps = new HashMap<String, Map<String, Object>>();
            tablesColumns.put(table.entity, columnMaps);
        }
        return columnMaps;
    }

    public Map<String, Object> getTableSettings(Table table) {
        return getColumnSettings(table, Config.COLUMN_OF_TABLE_ATTRS);
    }

    public Map<String, Object> getColumnSettings(Column column) {
        return getColumnSettings(column.table, column.varName, column.sqlName);
    }

    public Map<String, Object> getColumnSettings(Table table, String varName) {
        return getColumnSettings(table, varName, null);
    }

    public Map<String, Object> getColumnSettings(Table table, String varName, String sqlName) {
        final Map<String, Map<String, Object>> columnMap = getTableColumns(table);
        Map<String, Object> settings = columnMap.get(varName);
        if (settings == null) {
            settings = getOldColumnSettings(table, varName, sqlName);
            if (settings == null) {
                settings = new HashMap<String, Object>();
            }
            columnMap.put(varName, settings);
        }
        return settings;
    }

    public void eachTable(Arrays.Handler<Table> handler) {
        eachTable(handler, false);
    }

    public void eachTable(final Arrays.Handler<Table> handler, boolean includeBlankEntitys) {
        if (includeBlankEntitys) {
            Arrays.each(tables, handler);
        } else {
            Arrays.each(tables, new Arrays.Handler<Table>() {

                @Override
                public boolean each(int index, Table value) {
                    return handler.each(index, value);
                }
            });
        }
    }

    public void eachColumn(final Arrays.Handler<Column> handler) {
        eachColumn(handler, false);
    }

    public void eachColumn(final Arrays.Handler<Column> handler, boolean withBlankEntitys) {
        eachTable(new Arrays.Handler<Table>() {

            @Override
            public boolean each(int index, Table table) {
                return Arrays.each(table.getColumns(), handler);
            }
        }, withBlankEntitys);
    }

    public void eachTableColumnSettings(Maps.Handler<String, Map<String, Map<String, Object>>> handler) {
        Maps.each(this.tablesColumns, handler);
    }

    public void init() {
        this.tables = TableFactory.getTables();
        this.tablesColumns = new HashMap<String, Map<String, Map<String, Object>>>();
        this.tablesColumnsOld = ResourceUtil.loadTableColumns();
    }

    protected void beforeProcess() {
        eachColumn(new Arrays.Handler<Column>() {

            @Override
            public boolean each(final int index, final Column column) {
                Map<String, Object> columnMap = getColumnSettings(column);
                if (!columnMap.containsKey("query")) {
                    columnMap.put("query", "");
                }

                if (ResourceUtil.notValidValue(columnMap.get("fk")) && column.varName.endsWith("Id")) {
                    columnMap.put("fk", "");
                }

                return true;
            }
        });
        eachTable(new Arrays.Handler<Table>() {

            @Override
            public boolean each(int index, Table table) {
                //resure copy table settings
                getTableSettings(table);
                return true;
            }
        });
    }

    public void afterProcess() {
        //XXX: log
        ResourceUtil.saveTableColumns(this.tablesColumns);
    }

    public void process() throws IOException {
        init();
        beforeProcess();

        //ConfigInitProcesser
        final List<String> processersClass = StringUtil.toUnBlankList(Config.getString("configInit"));
        if (processersClass != null && !processersClass.isEmpty()) {
            try {
                for (String item : processersClass) {
                    //XXX: log
                    ((ConfigInitProcesser) ResourceUtil.loadClass(item).newInstance()).process(this);
                }
            } catch (Exception ex) {
                Logger.error("Exception: ", ex);
                throw new RuntimeException(ex);
            }
        }

        afterProcess();
    }

    public Map<String, Map<String, Map<String, Object>>> getTablesColumns() {
        return tablesColumns;
    }
}
