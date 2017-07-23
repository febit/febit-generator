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
package org.febit.generator.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.febit.generator.model.Table;
import org.febit.generator.util.Logger;
import org.febit.generator.util.ResourceUtil;
import org.febit.generator.util.dbaccess.DatabaseAccesser;
import org.febit.generator.util.dbaccess.TableRaw;

/**
 *
 * @author zqq90
 */
public abstract class TableFactory {

    private static TableFactory _instance;
    private static List<Table> _tables;
    private static Map<String, Table> _tableMap;

    protected abstract Table createTable(TableRaw tableRaw);

    public List<Table> collectTables() {

        final List<Table> tableList;
        {
            final Map<String, Table> tableMaps = _tableMap = new HashMap<>();
            for (TableRaw raw : DatabaseAccesser.getInstance().getAllTables()) {
                Table table = createTable(raw);
                if (table != null) {
                    tableMaps.put(table.entity, table);
                } else {
                    if (Logger.isDebugEnabled()) {
                        Logger.debug("Skip table (by TableFactory): " + raw);
                    }
                }
            }

            tableList = new ArrayList<>(tableMaps.values());
            //tables init
            for (Table table : tableList) {
                table.init();
            }
        }
        //table list sort
        Collections.sort(tableList);
        if (Logger.isInfoEnabled()) {
            for (Table table : tableList) {
                Logger.info("Loaded table: " + table.sqlName + "  " + table.remark);
            }
        }

        return tableList;
    }

    public static TableFactory instance() {
        TableFactory instance = _instance;
        if (instance == null) {
            instance = _instance = (TableFactory) ResourceUtil.loadComponent("tableFactory");
        }
        return instance;
    }

    public static List<Table> getTables() {
        List<Table> tables = _tables;
        if (tables == null) {
            tables = _tables = instance().collectTables();
        }
        return tables;
    }

    public static Map<String, Table> getTableMap() {
        Map<String, Table> tableMap = _tableMap;
        if (tableMap == null) {
            getTables();
            return getTableMap();
        }
        return tableMap;
    }

    public static Table getTable(String entity) {
        return getTableMap().get(entity);
    }

}
