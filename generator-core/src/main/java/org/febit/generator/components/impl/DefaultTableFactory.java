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

import java.util.Map;
import org.febit.generator.TableSettings;
import org.febit.generator.components.ColumnFactory;
import org.febit.generator.components.TableFactory;
import org.febit.generator.components.TableNaming;
import org.febit.generator.model.Column;
import org.febit.generator.model.Table;
import org.febit.generator.util.Logger;
import org.febit.generator.util.dbaccess.ColumnRaw;
import org.febit.generator.util.dbaccess.TableRaw;
import org.febit.util.ArraysUtil;

/**
 *
 * @author zqq90
 */
public class DefaultTableFactory extends TableFactory {

    protected TableNaming tableNaming;
    protected String[] blackEntitys;
    protected ColumnFactory columnFactory;
    protected TableSettings tableSettings;

    @Override
    protected Table createTable(final TableRaw tableRaw) {
        final String entity;
        final String sqlName;
        final String remark;
        final String modelType;
        final String modelSimpleType;
        final boolean blackEntity;
        final Map<String, Object> tableAttrs;

        remark = tableNaming.remark(tableRaw.remark);

        sqlName = tableNaming.sqlName(tableRaw.name);
        entity = tableNaming.entity(sqlName);

        blackEntity = ArraysUtil.contains(blackEntitys, entity);
        modelSimpleType = tableNaming.modelSimpleType(entity);
        modelType = tableNaming.modelType(modelSimpleType);
        tableAttrs = tableSettings.getTableAttrs(entity);
        final Table table = new Table(entity, sqlName, remark, tableAttrs, modelType, modelSimpleType, blackEntity);
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
