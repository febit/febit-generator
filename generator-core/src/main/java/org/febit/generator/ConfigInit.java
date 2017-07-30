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
import java.util.List;
import java.util.Map;
import org.febit.generator.components.ConfigInitProcesser;
import org.febit.generator.components.TableFactory;
import org.febit.generator.model.Column;
import org.febit.generator.model.Table;
import org.febit.generator.util.Arrays;
import org.febit.generator.util.Logger;
import org.febit.lang.Function2;
import org.febit.lang.Singleton;

/**
 *
 * @author zqq90
 */
public class ConfigInit implements Singleton {

    protected ConfigInitProcesser[] processers;

    protected TableFactory tableFactory;
    protected TableSettings tableSettings;

    private List<Table> tables;
    private TableSettings.Tables settings;
    private TableSettings.Tables settingsOld;

    public void init() {
        this.tables = tableFactory.getTables();
        this.settings = new TableSettings.Tables();
        this.settingsOld = tableSettings.getSettings();
    }

    public void eachTable(Arrays.Handler<Table> handler) {
        eachTable(handler, false);
    }

    public void eachTable(final Arrays.Handler<Table> handler, boolean includeBlankEntitys) {
        if (includeBlankEntitys) {
            Arrays.each(tables, handler);
        } else {
            Arrays.each(tables, (Table value) -> {
                if (value.isBlackEntity) {
                    return true;
                }
                return handler.each(value);
            });
        }
    }

    public void eachColumn(final Arrays.Handler<Column> handler) {
        eachColumn(handler, false);
    }

    public void eachColumn(final Arrays.Handler<Column> handler, boolean withBlankEntitys) {
        eachTable((Table table) -> Arrays.each(table.columns, handler), withBlankEntitys);
    }

    public void eachColumnsSettings(Function2<Boolean, String, TableSettings.Columns> handler) {
        for (Map.Entry<String, TableSettings.Columns> entry : settings.entrySet()) {
            if (!handler.call(entry.getKey(), entry.getValue())) {
                return;
            }
        }
    }

    protected void beforeProcess() {
        eachColumn((Column column) -> {
            TableSettings.ColumnAttrs columnMap = settings.getColumnAttrs(column);
            if (columnMap.isEmpty()) {
                // merge old settings
                columnMap.putAll(this.settingsOld.getColumnAttrs(column));
            }
            if (!columnMap.containsKey("query")) {
                columnMap.put("query", null);
            }
            if (!columnMap.containsKey("fk") && column.name.endsWith("Id")) {
                columnMap.put("fk", null); //XXX: 可推断
            }
            return true;
        });
        eachTable((Table table) -> {
            Map<String, Object> tableAttrs = this.settings.getTableAttrs(table);
            if (tableAttrs.isEmpty()) {
                // merge old settings
                tableAttrs.putAll(this.settingsOld.getTableAttrs(table));
            }
            return true;
        });
    }

    public void afterProcess() {
        tableSettings.saveSettings(this.settings);
    }

    public void process() throws IOException {
        init();
        beforeProcess();
        for (ConfigInitProcesser processer : processers) {
            Logger.info("Running processer: " + processer.getClass());
            processer.process(this);
        }
        afterProcess();
        //TODO: log summary
    }

    public TableSettings.Tables getSettings() {
        return settings;
    }
}
