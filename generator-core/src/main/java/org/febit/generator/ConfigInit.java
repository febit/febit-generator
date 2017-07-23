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
package org.febit.generator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.febit.generator.components.ConfigInitProcesser;
import org.febit.generator.components.TableFactory;
import org.febit.generator.model.Column;
import org.febit.generator.model.Table;
import org.febit.generator.util.Arrays;
import org.febit.generator.util.Logger;
import org.febit.generator.util.Maps;
import org.febit.generator.util.ResourceUtil;

/**
 *
 * @author zqq90
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
        return getOldColumnSettings(column.table, column.name, column.sqlName);
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
            columnMaps = new HashMap<>();
            tablesColumns.put(table.entity, columnMaps);
        }
        return columnMaps;
    }

    public Map<String, Object> getTableSettings(Table table) {
        return getColumnSettings(table, Config.COLUMN_OF_TABLE_ATTRS);
    }

    public Map<String, Object> getColumnSettings(Column column) {
        return getColumnSettings(column.table, column.name, column.sqlName);
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
                settings = new HashMap<>();
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
                private int count;

                @Override
                public boolean each(int index, Table value) {
                    if (value.isBlackEntity) {
                        return true;
                    }
                    return handler.each(count++, value);
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
                return Arrays.each(table.columns, handler);
            }
        }, withBlankEntitys);
    }

    public void eachTableColumnSettings(Maps.Handler<String, Map<String, Map<String, Object>>> handler) {
        Maps.each(this.tablesColumns, handler);
    }

    public void init() {
        this.tables = TableFactory.getTables();
        this.tablesColumns = new HashMap<>();
        this.tablesColumnsOld = ResourceUtil.loadTableColumns();
    }

    protected void beforeProcess() {
        eachColumn(new Arrays.Handler<Column>() {

            @Override
            public boolean each(final int index, final Column column) {
                Map<String, Object> columnMap = getColumnSettings(column);
                if (!columnMap.containsKey("query")) {
                    columnMap.put("query", null);
                }

                if (!columnMap.containsKey("fk") && column.name.endsWith("Id")) {
                    columnMap.put("fk", null); //XXX: 可推断
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
        ResourceUtil.saveTableColumns(this.tablesColumns);
        Logger.info("Saved " + ResourceUtil.getResPath(ResourceUtil.COLUMNS_PROPS));
    }

    public void process() throws IOException {
        init();
        beforeProcess();

        //ConfigInitProcessers
        try {
            for (String item : Config.getArrayWithoutComment("configInit")) {
                Logger.info("Running processer: " + item);
                ((ConfigInitProcesser) ResourceUtil.loadClass(item).newInstance()).process(this);
            }
        } catch (Exception ex) {
            Logger.error("Exception: ", ex);
            throw new RuntimeException(ex);
        }

        afterProcess();
        //TODO: log summary
    }

    public Map<String, Map<String, Map<String, Object>>> getTablesColumns() {
        return tablesColumns;
    }
}
