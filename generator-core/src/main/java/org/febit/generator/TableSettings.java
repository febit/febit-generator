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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.TreeMap;
import jodd.io.StreamUtil;
import org.febit.generator.components.TableFactory;
import org.febit.generator.model.Column;
import org.febit.generator.model.Table;
import org.febit.generator.util.CommonUtil;
import org.febit.generator.util.FileUtil;
import org.febit.generator.util.Logger;
import org.febit.lang.Singleton;

/**
 *
 * @author zqq90
 */
public class TableSettings implements Singleton {

    public static final String COLUMN_OF_TABLE_ATTRS = "$";

    public static class Attrs extends TreeMap<String, Object> {

        public String getValidString(String key) {
            Object val = get(key);
            if (val == null || "".equals(val)) {
                return null;
            }
            return val.toString();
        }

        public Object getValid(String key) {
            Object val = get(key);
            if (val == null || "".equals(val)) {
                return null;
            }
            return val;
        }

        public boolean getBoolean(String key) {
            return CommonUtil.toBoolean(get(key), false);
        }
    }

    public static class Columns extends TreeMap<String, Attrs> {

        @Override
        @Deprecated
        public Attrs get(Object key) {
            return super.get(key);
        }

        public Attrs get(Column column) {
            return get(column.name);
        }

        public Attrs get(String key) {
            Attrs val = super.get(key);
            if (val == null) {
                val = new Attrs();
                put(key, val);
            }
            return val;
        }

    }

    public static class Tables extends TreeMap<String, Columns> {

        public Attrs getColumnAttrs(Column column) {
            return get(column.table).get(column);
        }

        public Attrs getColumnAttrs(String table, String column) {
            return get(table).get(column);
        }

        public Attrs getColumnAttrs(Table table, String column) {
            return get(table).get(column);
        }

        public Attrs getTableAttrs(String entity) {
            return getColumnAttrs(entity, TableSettings.COLUMN_OF_TABLE_ATTRS);
        }

        public Attrs getTableAttrs(Table table) {
            return getTableAttrs(table.entity);
        }

        public Columns get(Table table) {
            return get(table.entity);
        }

        public Columns get(String key) {
            Columns val = super.get(key);
            if (val == null) {
                val = new Columns();
                put(key, val);
            }
            return val;
        }
    }

    protected String propsPath;

    private Tables settings;

    public Tables getSettings() {
        if (settings == null) {
            settings = loadSettings(propsPath);
        }
        return settings;
    }

    public void saveSettings(final Tables tableColumnsMap) {
        saveSettings(tableColumnsMap, propsPath);
    }

    public Columns getColumns(String entity) {
        return getSettings().get(entity);
    }

    public Attrs getTableAttrs(String entity) {
        return getSettings().getTableAttrs(entity);
    }

    public Attrs getTableAttrs(Table table) {
        return getSettings().getTableAttrs(table);
    }

    public Attrs getColumnAttrs(Table table, String varName) {
        return getSettings().getColumnAttrs(table, varName);
    }

    public Attrs getColumnAttrs(String entity, String varName) {
        return getSettings().getColumnAttrs(entity, varName);
    }

    public static Object toValidValue(Object value) {
        return "".equals(value) ? null : value;
    }

    protected static Tables loadSettings(String path) {
        final Tables result = new Tables();
        FileUtil.loadResource(path).entrySet().forEach((entry) -> {
            String key = entry.getKey();
            int index = key.indexOf('.');
            if (!(index <= 0)) {
                int index2 = key.indexOf('.', index + 1);
                final String table = key.substring(0, index);
                final String column;
                final String property;
                if (index2 < 0) {
                    column = COLUMN_OF_TABLE_ATTRS;
                    property = key.substring(index + 1);
                } else {
                    column = key.substring(index + 1, index2);
                    property = key.substring(index2 + 1);
                }
                TableSettings.Attrs columnPropertys = result.getColumnAttrs(table, column);
                columnPropertys.put(property, toValidValue(entry.getValue()));
            }
        });
        return result;
    }

    public static void saveSettings(final Tables tableColumnsMap, String path) {
        final File file = new File(path);
        FileUtil.backupResourceIfExists(file);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
            for (Map.Entry<String, TableSettings.Columns> entry : tableColumnsMap.entrySet()) {
                final String entity = entry.getKey();
                final Table table = Lazy.get(TableFactory.class).getTable(entity);
                writer.append("\n\n### ").append(table.remark).append('\n');
                writer.append('[').append(table.entity).append(']').append("\n\n");
                for (Map.Entry<String, TableSettings.Attrs> entry1 : entry.getValue().entrySet()) {
                    final String varName = entry1.getKey();
                    if (COLUMN_OF_TABLE_ATTRS.equals(varName)) {
                        for (Map.Entry<String, Object> entry2 : entry1.getValue().entrySet()) {
                            Object value = entry2.getValue();
                            writer.append(entry2.getKey()).append('=').append(value != null ? String.valueOf(value) : "").append('\n');
                        }
                    } else {
                        final Column column = table.getColumnByName(varName);
                        writer.append("# ").append(column.getRemark()).append('\n');
                        for (Map.Entry<String, Object> entry2 : entry1.getValue().entrySet()) {
                            Object value = entry2.getValue();
                            writer.append(column.name).append('.').append(entry2.getKey()).append('=').append(value != null ? String.valueOf(value) : "").append('\n');
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            StreamUtil.close(writer);
        }

        Logger.info("Saved tableSettings: " + path);
    }
}
