// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import webit.generator.Config;
import webit.generator.components.TableFactory;
import webit.generator.model.Column;
import webit.generator.model.Table;
import org.febit.wit.util.Props;

/**
 *
 * @author Zqq
 */
public class ResourceUtil {

    public final static String COLUMNS_PROPS = "columns.props";

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
                    column = Config.COLUMN_OF_TABLE_ATTRS;
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
            loadFormFile(props = createProps(), ResourceUtil.getResPath(filename));
        } catch (IOException ex) {
            //Logger.error("IOException of file: " + getResPath(filename), ex);
            //throw new RuntimeException(ex);
            return null;
        }
        final Map<String, String> data;
        props.extractTo(data = new HashMap<String, String>());
        return data;
    }

    public static void backupResourceIfExists(final File file) {
        if (file.exists()) {
            file.renameTo(new File(file.getPath() + '.' + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".bak"));
        }
    }

    public static void saveTableColumns(final Map<String, Map<String, Map<String, Object>>> tableColumnsMap) {
        final File file = new File(getResPath(COLUMNS_PROPS));
        backupResourceIfExists(file);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
            for (Map.Entry<String, Map<String, Map<String, Object>>> entry : new TreeMap<String, Map<String, Map<String, Object>>>(tableColumnsMap).entrySet()) {
                final String entity = entry.getKey();
                final Table table = TableFactory.getTable(entity);
                writer.append("\n\n### ").append(table.remark).append('\n');
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

    public static void loadFormFile(Props props, String fileName) throws IOException {
        props.load(FileUtil.readChars(fileName));
    }

    public static Props createFromClasspath(String fileName) {
        final Props props = createProps();
        loadFormClasspath(props, fileName);
        return props;
    }

    public static Props createProps() {
        return new Props();
    }

    public static boolean loadFormClasspath(Props props, String fileName) {
        final char[] data = ResourceUtil.readCharsFromClasspath(fileName);
        if (data != null) {
            props.load(data);
            return true;
        }
        return false;
    }

}
