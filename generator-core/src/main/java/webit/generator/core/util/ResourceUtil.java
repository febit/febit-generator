// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import webit.generator.core.Config;
import webit.generator.core.components.TableFactory;
import webit.generator.core.model.Column;
import webit.generator.core.model.Table;
import webit.script.util.props.Props;

/**
 *
 * @author Zqq
 */
public class ResourceUtil {

    private final static String COLUMNS_PROPS = "columns.props";

    public static Map<String, Map<String, Map<String, Object>>> loadTableColumns() {
        final Map<String, String> data = loadResource(COLUMNS_PROPS);
        final Map<String, Map<String, Map<String, Object>>> result = new HashMap<String, Map<String, Map<String, Object>>>();
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String key = entry.getKey();
                int index = key.indexOf('.');
                if (index <= 0) {
                    continue;
                }
                int index2 = key.indexOf('.', index + 1);
                final String table = key.substring(0, index);
                final String column;
                final String property;
                if (index2 < 0) {
                    column = "$";
                    property = key.substring(index + 1);
                } else {
                    column = key.substring(index + 1, index2);
                    property = key.substring(index2 + 1);
                }

                Map<String, Map<String, Object>> tableColumns = result.get(table);
                if (tableColumns == null) {
                    result.put(table, tableColumns = new HashMap<String, Map<String, Object>>());
                }

                Map<String, Object> columnPropertys = tableColumns.get(column);

                if (columnPropertys == null) {
                    tableColumns.put(column, columnPropertys = new HashMap<String, Object>());
                }
                columnPropertys.put(property, toValidValue(entry.getValue()));
            }
        }
        return result;
    }

    public static Map<String, String> loadResource(String filename) {
        final Props props;
        try {
            PropsUtil.loadFormFile(props = PropsUtil.createProps(), getResPath(filename));
        } catch (IOException ex) {
            //Logger.error("IOException of file: " + getResPath(filename), ex);
            //throw new RuntimeException(ex);
            return null;
        }
        final Map<String, String> data;
        props.extractProps(data = new HashMap<String, String>());
        return data;
    }

    public static void saveTableColumns(final Map<String, Map<String, Map<String, Object>>> tableColumnsMap) {
        final File file = new File(getResPath(COLUMNS_PROPS));
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
            for (Map.Entry<String, Map<String, Map<String, Object>>> entry : new TreeMap<String, Map<String, Map<String, Object>>>(tableColumnsMap).entrySet()) {
                final String entity = entry.getKey();
                final Table table = TableFactory.getTable(entity);
                writer.append("\n\n### ").append(table.getRemark()).append('\n');
                writer.append('[').append(table.entity).append(']').append("\n\n");
                for (Map.Entry<String, Map<String, Object>> entry1 : new TreeMap<String, Map<String, Object>>(entry.getValue()).entrySet()) {
                    final String varName = entry1.getKey();
                    if (Config.COLUMN_OF_TABLE_ATTRS.equals(varName)) {
                        for (Map.Entry<String, Object> entry2 : new TreeMap<String, Object>(entry1.getValue()).entrySet()) {
                            Object value = entry2.getValue();
                            writer.append(entry2.getKey()).append('=').append(value != null ? String.valueOf(value) : "").append('\n');
                        }
                    } else {
                        final Column column = table.getColumnByName(varName);
                        writer.append("# ").append(column.getRemark()).append('\n');
                        for (Map.Entry<String, Object> entry2 : new TreeMap<String, Object>(entry1.getValue()).entrySet()) {
                            Object value = entry2.getValue();
                            writer.append(column.varName).append('.').append(entry2.getKey()).append('=').append(value != null ? String.valueOf(value) : "").append('\n');
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            StreamUtil.close(writer);
        }
    }

    public static String getResPath(String name) {
        return FileUtil.concat(Config.getWorkPath(), name);
    }

    public static ClassLoader getDefaultClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static char[] readCharsFromClasspath(final String name) {
        final InputStream in = getDefaultClassLoader().getResourceAsStream(name);
        if (in != null) {
            try {
                return StreamUtil.readChars(in);
            } catch (IOException ignore) {
                Logger.warn("Failed to read chars from classpath: " + name, ignore);
            } finally {
                StreamUtil.close(in);
            }
        }
        return null;
    }

    public static Class loadComponentClass(String key) {
        try {
            return ResourceUtil.loadClass(Config.getRequiredString(key));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object loadComponent(String key) {
        try {
            return loadComponentClass(key).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class loadClass(String className) throws ClassNotFoundException {
        return getDefaultClassLoader().loadClass(className);
    }

    public static Object toValidValue(Object value) {
        return "".equals(value) ? null : value;
    }

}
