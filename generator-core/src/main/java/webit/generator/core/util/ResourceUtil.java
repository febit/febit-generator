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
                if (index2 <= 0) {
                    continue;
                }
                String table = key.substring(0, index);
                String column = key.substring(index + 1, index2);
                String property = key.substring(index2 + 1);
                Map<String, Map<String, Object>> tableColumns = result.get(table);
                if (tableColumns == null) {
                    result.put(table, tableColumns = new HashMap<String, Map<String, Object>>());
                }

                Map<String, Object> columnPropertys = tableColumns.get(column);

                if (columnPropertys == null) {
                    tableColumns.put(column, columnPropertys = new HashMap<String, Object>());
                }
                columnPropertys.put(property, entry.getValue());
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

    public static void saveTableColumns(final Map<Table, Map<Column, Map<String, Object>>> tableColumnsMap) {
        final File file = new File(getResPath(COLUMNS_PROPS));
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
            for (Map.Entry<Table, Map<Column, Map<String, Object>>> entry : new TreeMap<Table, Map<Column, Map<String, Object>>>(tableColumnsMap).entrySet()) {
                final Table table = entry.getKey();
                writer.append("\n\n### ").append(table.getRemark()).append('\n');
                writer.append('[').append(table.entity).append(']').append("\n\n");
                for (Map.Entry<Column, Map<String, Object>> entry1 : new TreeMap<Column, Map<String, Object>>(entry.getValue()).entrySet()) {
                    final Column column = entry1.getKey();
                    final Map<String, Object> sortedPropertys = new TreeMap<String, Object>(entry1.getValue());
                    writer.append("# ").append(column.getRemark()).append('\n');
                    for (Map.Entry<String, Object> entry2 : sortedPropertys.entrySet()) {
                        writer.append(column.varName).append('.').append(entry2.getKey()).append('=').append(entry2.getValue().toString()).append('\n');
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

    public static boolean validValue(Object value) {
        return value != null && !value.equals("");
    }

    public static boolean notValidValue(Object value) {
        return !validValue(value);
    }
}
