// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Map<Table, Map<Column, Map<String, Object>>> tableColumn;
    private Map<String, Map<String, Map<String, Object>>> tableColumnOld;

    public Map<String, Map<String, Object>> getOldColumnMaps(String tableName) {
        return tableColumnOld != null ? tableColumnOld.get(tableName) : null;
    }

    public Map<String, Map<String, Object>> getOldColumnMaps(Table table) {
        Map<String, Map<String, Object>> result = getOldColumnMaps(table.entity);
        if (result == null) {
            result = getOldColumnMaps(table.sqlName);
        }
        return result;
    }

    public Map<String, Object> getOldColumnMap(Column column) {
        Map<String, Map<String, Object>> oldColumnMaps = getOldColumnMaps(column.getTable());
        if (oldColumnMaps == null) {
            return null;
        }
        Map<String, Object> result = oldColumnMaps.get(column.varName);
        if (result == null) {
            result = oldColumnMaps.get(column.sqlName);
        }
        return result;
    }

    public Map<Column, Map<String, Object>> getColumnMaps(Table table) {
        Map<Column, Map<String, Object>> columnMaps = tableColumn.get(table);
        if (columnMaps == null) {
            columnMaps = new HashMap<Column, Map<String, Object>>();
            tableColumn.put(table, columnMaps);
        }
        return columnMaps;
    }

    public Map<String, Object> getColumnMap(Column column) {
        final Map<Column, Map<String, Object>> newColumnMaps = getColumnMaps(column.getTable());
        Map<String, Object> columnMap = newColumnMaps.get(column);
        if (columnMap == null) {
            columnMap = getOldColumnMap(column);
            if (columnMap == null) {
                columnMap = new HashMap<String, Object>();
            }
            newColumnMaps.put(column, columnMap);
        }
        return columnMap;
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

    public void eachColumnMaps(Maps.Handler<Table, Map<Column, Map<String, Object>>> handler) {
        Maps.each(this.tableColumn, handler);
    }

    public void init() {
        this.tables = TableFactory.getTables();
        this.tableColumn = new HashMap<Table, Map<Column, Map<String, Object>>>();
        this.tableColumnOld = ResourceUtil.loadTableColumns();
    }

    protected void beforeProcess() {
        eachColumn(new Arrays.Handler<Column>() {
            
            @Override
            public boolean each(final int index, final Column column) {
                Map<String, Object> columnMap = getColumnMap(column);
                if (!columnMap.containsKey("query")) {
                    columnMap.put("query", "");
                }
                return true;
            }
        });
    }

    public void afterProcess() {
        //XXX: log
        ResourceUtil.saveTableColumns(this.tableColumn);
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

    public List<Table> getTables() {
        return tables;
    }

    public Map<Table, Map<Column, Map<String, Object>>> getTableColumn() {
        return tableColumn;
    }
}
