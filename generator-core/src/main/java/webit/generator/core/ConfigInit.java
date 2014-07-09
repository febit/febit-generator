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
    private Map<String, Map<String, Map<String, Object>>> tableColumn;
    private Map<String, Map<String, Map<String, Object>>> tableColumnOld;

    public Map<String, Map<String, Object>> getOldColumnMaps(String tableName) {
        return tableColumnOld != null ? tableColumnOld.get(tableName) : null;
    }

    public Map<String, Map<String, Object>> getOldColumnMaps(Table table) {
        return getOldColumnMaps(table.getSqlName());
    }

    public Map<String, Object> getOldColumnMap(Column column) {
        Map<String, Map<String, Object>> oldColumnMaps = getOldColumnMaps(column.getTable());
        return oldColumnMaps != null ? oldColumnMaps.get(column.getSqlName()) : null;
    }

    public Map<String, Map<String, Object>> getColumnMaps(String tableName) {
        Map<String, Map<String, Object>> columnMaps = tableColumn.get(tableName);
        if (columnMaps == null) {
            columnMaps = new HashMap<String, Map<String, Object>>();
            tableColumn.put(tableName, columnMaps);
        }
        return columnMaps;
    }

    public Map<String, Map<String, Object>> getColumnMaps(Table table) {
        return ConfigInit.this.getColumnMaps(table.getSqlName());
    }

    public Map<String, Object> getColumnMap(Column column) {
        Map<String, Map<String, Object>> newColumnMaps = getColumnMaps(column.getTable());
        Map<String, Object> columnMap = newColumnMaps.get(column.getSqlName());
        if (columnMap == null) {
            columnMap = getOldColumnMap(column);
            if (columnMap == null) {
                columnMap = new HashMap<String, Object>();
            }
            newColumnMaps.put(column.getSqlName(), columnMap);
        }
        return columnMap;
    }

    public void eachTable(Arrays.Handler<Table> handler) {
        Arrays.each(tables, handler);
    }

    public void eachColumn(final Arrays.Handler<Column> handler) {
        eachTable(new Arrays.Handler<Table>() {
            public boolean each(int index, Table table) {
                return Arrays.each(table.getColumns(), handler);
            }
        });
    }

    public void eachColumnMaps(Maps.Handler<String, Map<String, Map<String, Object>>> handler) {
        Maps.each(this.tableColumn, handler);
    }

    public void init() {
        this.tables = TableFactory.getTables();
        this.tableColumn = new HashMap<String, Map<String, Map<String, Object>>>();
        this.tableColumnOld = ResourceUtil.loadTableColumns();
    }

    protected void beforeProcess() {
        eachColumn(new Arrays.Handler<Column>() {
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

    public Map<String, Map<String, Map<String, Object>>> getTableColumn() {
        return tableColumn;
    }

    public Map<String, Map<String, Map<String, Object>>> getTableColumnOld() {
        return tableColumnOld;
    }
}
