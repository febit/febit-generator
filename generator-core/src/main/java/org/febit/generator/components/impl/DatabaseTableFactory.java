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
import java.util.List;
import org.febit.generator.Lazy;
import org.febit.generator.TableSettings;
import org.febit.generator.TableSettings.Attrs;
import org.febit.generator.components.TableFactory;
import org.febit.generator.components.TableNaming;
import org.febit.generator.model.Column;
import org.febit.generator.model.Table;
import org.febit.generator.util.Logger;
import org.febit.generator.util.dbaccess.ColumnRaw;
import org.febit.generator.util.dbaccess.DatabaseAccesser;
import org.febit.generator.util.dbaccess.TableRaw;
import org.febit.util.ArraysUtil;

/**
 *
 * @author zqq90
 */
public class DatabaseTableFactory extends TableFactory {

    protected String[] blackEntitys;

    protected TableNaming tableNaming;
    protected TableSettings tableSettings;
    protected DatabaseColumnFactory columnFactory;

    @Override
    protected List<Table> collectTables() {
        List<Table> tableList = new ArrayList<>();
        createDatabaseAccesser().getAllTables().forEach((raw) -> {
            Table table = createTable(raw);
            if (table == null) {
                Logger.debug("Skip table (by TableFactory): {}", raw);
                return;
            }
            tableList.add(table);
        });
        return tableList;
    }

    protected DatabaseAccesser createDatabaseAccesser() {
        DatabaseAccesser accesser = new DatabaseAccesser();
        Lazy.petite().inject("db", accesser);
        return accesser;
    }

    protected Table createTable(final TableRaw tableRaw) {
        final String entity;
        final String sqlName;
        final String remark;
        final String modelType;
        final boolean blackEntity;
        final Attrs tableAttrs;

        sqlName = tableNaming.sqlName(tableRaw.name);
        remark = tableNaming.remark(tableRaw.remark);
        entity = tableNaming.entity(sqlName);

        blackEntity = ArraysUtil.contains(blackEntitys, entity);
        modelType = tableNaming.modelType(entity);
        tableAttrs = tableSettings.getTableAttrs(entity);

        final Table table = new Table(tableAttrs, sqlName, entity, modelType, blackEntity, remark);
        for (ColumnRaw columnRaw : tableRaw.getColumns()) {
            Column col = columnFactory.create(columnRaw, table);
            if (col == null) {
                Logger.debug("Skip column (by ColumnFactory): {}", columnRaw);
                continue;
            }
            table.addColumn(col);
        }
        return table;
    }
}
