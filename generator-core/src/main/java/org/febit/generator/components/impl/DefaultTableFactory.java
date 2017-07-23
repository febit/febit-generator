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
package org.febit.generator.components.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.febit.generator.Config;
import org.febit.generator.components.ColumnFactory;
import org.febit.generator.components.TableFactory;
import org.febit.generator.components.TableNaming;
import org.febit.generator.model.Column;
import org.febit.generator.model.Table;
import org.febit.generator.util.Logger;
import org.febit.generator.util.dbaccess.ColumnRaw;
import org.febit.generator.util.dbaccess.TableRaw;

/**
 *
 * @author zqq90
 */
public class DefaultTableFactory extends TableFactory {

    private final Set<String> blackEntitys = new HashSet<>();

    private boolean inited = false;

    public void init() {
        if (inited == true) {
            return;
        }
        inited = true;

        //加载表黑名单
        blackEntitys.addAll(Arrays.asList(Config.getArrayWithoutComment("blackEntitys")));
    }

    @Override
    protected Table createTable(final TableRaw tableRaw) {
        init();
        final String entity;
        final String sqlName;
        final String remark;
        final ArrayList<Column> columns;
        final ArrayList<Column> idColumns;
        final Map<String, Column> columnMap;
        final String modelType;
        final String modelSimpleType;
        final boolean blackEntity;
        final Map<String, Object> tableSettings;

        final TableNaming tableNaming = TableNaming.instance();

        remark = tableNaming.remark(tableRaw.remark);
        columns = new ArrayList<>();
        idColumns = new ArrayList<>();
        columnMap = new HashMap<>();

        sqlName = tableNaming.sqlName(tableRaw.name);
        entity = tableNaming.entity(sqlName);

        blackEntity = blackEntitys.contains(entity);
        modelSimpleType = tableNaming.modelSimpleType(entity);
        modelType = tableNaming.modelType(modelSimpleType);
        tableSettings = Config.getTableSettings(entity);
        final Table table = new Table(entity, sqlName, remark, tableSettings, columnMap, columns, idColumns, modelType, modelSimpleType, blackEntity);
        {
            //XXX: tableSettings.get("id")
            for (ColumnRaw columnRaw : tableRaw.getColumns()) {
                Column cm = ColumnFactory.create(columnRaw, table);
                if (cm == null) {
                    if (Logger.isDebugEnabled()) {
                        Logger.debug("Skip column (by ColumnFactory): " + columnRaw);
                    }
                    continue;
                }
                if (cm.getIspk()) {
                    idColumns.add(cm);
                }
                columnMap.put(cm.getVarName(), cm);
            }
            columns.addAll(columnMap.values());
            idColumns.trimToSize();
            columns.trimToSize();
            Collections.sort(columns);
        }

        return table;
    }
}
