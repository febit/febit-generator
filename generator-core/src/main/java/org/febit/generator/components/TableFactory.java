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
import org.febit.generator.util.dbaccess.DatabaseAccesser;
import org.febit.generator.util.dbaccess.TableRaw;
import org.febit.lang.Singleton;
import org.febit.util.agent.LazyAgent;

/**
 *
 * @author zqq90
 */
public abstract class TableFactory implements Singleton {

    private final LazyAgent<List<Table>> _tables = LazyAgent.create(() -> {
        List<Table> tableList = collectTables();
        // sort
        Collections.sort(tableList);
        //tables init
        tableList.forEach((table) -> {
            table.init();
            Logger.info("Loaded table: " + table.sqlName + "  " + table.remark);
        });
        return Collections.unmodifiableList(tableList);
    });

    private final LazyAgent<Map<String, Table>> _tableMap = LazyAgent.create(() -> {
        Map<String, Table> tableMaps = new HashMap<>();
        _tables.get().forEach(((table) -> {
            // check conflict
            if (tableMaps.containsKey(table.sqlName)) {
                Logger.error("conflict tables in sql name: {} vs. {}", tableMaps.get(table.sqlName), table);
            }
            if (tableMaps.containsKey(table.entity)) {
                Logger.error("conflict tables in entity name: {} vs. {}", tableMaps.get(table.entity), table);
            }
            tableMaps.put(table.sqlName, table);
            tableMaps.put(table.entity, table);
        }));
        return Collections.unmodifiableMap(tableMaps);
    });

    protected abstract List<Table> collectTables();

    public List<Table> getTables() {
        return _tables.get();
    }

    public Map<String, Table> getTableMap() {
        return _tableMap.get();
    }

    /**
     * get table by entity name or sql name.
     *
     * @param entity or sql name
     * @return cached table
     */
    public Table getTable(String entity) {
        return _tableMap.get().get(entity);
    }

}
