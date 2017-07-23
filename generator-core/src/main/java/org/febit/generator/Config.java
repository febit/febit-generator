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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.febit.generator.model.DependLib;
import org.febit.generator.model.Table;
import org.febit.generator.util.CommonUtil;
import org.febit.generator.util.Logger;
import org.febit.generator.util.ResourceUtil;
import org.febit.util.StringUtil;
import org.febit.wit.util.Props;

public class Config {

    public static final String COLUMN_OF_TABLE_ATTRS = "$";
    private static final String DEFAULT_PROPS = "default.props";
    private static final String PROPS_MODULES = "modules";

    public static final ArrayList<String> MODULES = new ArrayList<String>();

    private static final Map<String, String> configs = new HashMap<String, String>();
    private static final List<String> commonTemplates = new ArrayList<String>();
    private static final List<String> tableTemplates = new ArrayList<String>();
    private static final Map<String, String> fileTypeMap = new HashMap<String, String>();
    private static final TreeSet<DependLib> depends = new TreeSet<DependLib>();
    private static final TreeSet<DependLib> testDepends = new TreeSet<DependLib>();
    private static final TreeSet<DependLib> providedDepends = new TreeSet<DependLib>();

    private static Props props;
    private static String workPath;
    private static boolean loadedDefault = false;

    private static void initalize() {
        configs.clear();
        props.extractTo(configs);
        //fileTypes
        {
            fileTypeMap.clear();
            for (String item : getArrayWithoutComment("filetypes")) {
                int index = item.indexOf('=');
                if (index > 0) {
                    fileTypeMap.put(item.substring(0, index).trim(), item.substring(index + 1).trim());
                } else {
                    throw buildException("filetypes lost '=',must be 'key = value: " + item);
                }
            }
        }

        //tmpls
        {
            tableTemplates.clear();
            commonTemplates.clear();
            for (String item : getArrayWithoutComment("tmpls")) {
                if (item.endsWith(".each")) {
                    tableTemplates.add(item);
                } else {
                    commonTemplates.add(item);
                }
            }
        }

        //depends
        {
            resolveDepends(depends, "depends");
            resolveDepends(testDepends, "testDepends");
            resolveDepends(providedDepends, "providedDepends");
        }
    }

    private static void resolveDepends(TreeSet<DependLib> dependSet, String propsName) {
        dependSet.clear();
        for (String item : getArrayWithoutComment(propsName)) {
            dependSet.add(DependLib.valueOf(item));
        }
        //check & remove same artifact, keep higher-version one
        final Iterator<DependLib> it = dependSet.descendingIterator();
        if (it.hasNext()) {
            DependLib preDepend = it.next();
            while (it.hasNext()) {
                DependLib dependLib = it.next();
                if (dependLib.isSameArtifact(preDepend)) {
                    it.remove();
                } else {
                    preDepend = dependLib;
                }
            }
        }
    }

    private static void loadDefault() {
        if (!loadedDefault) {
            loadedDefault = true;
            MODULES.clear();
            props = ResourceUtil.createFromClasspath(DEFAULT_PROPS);
            resolveModules("core");
        }
    }

    public static void load(String filename) {
        loadDefault();
        if (filename != null) {
            try {
                Props moduleProps = ResourceUtil.createProps();
                ResourceUtil.loadFormFile(moduleProps, filename);
                resolveModules(moduleProps.get(PROPS_MODULES));
                props.merge(moduleProps);
            } catch (IOException ex) {
                throw buildException("Not found props file: " + filename);
            }
        }
        initalize();
    }

    private static Map<String, Map<String, Map<String, Object>>> tablesSettings;

    public static Map<String, Map<String, Map<String, Object>>> getTablesSettings() {
        Map<String, Map<String, Map<String, Object>>> settings = tablesSettings;
        if (settings == null) {
            settings = tablesSettings = ResourceUtil.loadTableColumns();
        }
        return settings;
    }

    public static Map<String, Map<String, Object>> getTableColumnSettings(String entity) {
        Map<String, Map<String, Object>> settings = getTablesSettings().get(entity);
        if (settings == null) {
            settings = new HashMap<>();
            getTablesSettings().put(entity, settings);
        }
        return settings;
    }

    public static Map<String, Object> getTableSettings(String entity) {
        return getColumnSettings(entity, COLUMN_OF_TABLE_ATTRS);
    }

    public static Map<String, Object> getTableSettings(Table table) {
        return getTableSettings(table.entity);
    }

    public static Map<String, Object> getColumnSettings(Table table, String varName) {
        return getColumnSettings(table.entity, varName);
    }

    public static Map<String, Object> getColumnSettings(String entity, String varName) {
        Map<String, Object> settings = getTableColumnSettings(entity).get(varName);
        if (settings == null) {
            settings = new HashMap<>();
            getTableColumnSettings(entity).put(varName, settings);
        }
        return settings;
    }

    public static boolean isModuleActived(String name) {
        return MODULES.contains(name);
    }

    private final static Map<String, Props> MODILES_PROPS = new HashMap<String, Props>();

    private static void resolveModules(final String names) {
        if (names != null) {
            for (String depend : StringUtil.toArrayExcludeCommit(names)) {
                if (MODULES.contains(depend)) {
                    continue;
                }
                Props moduleProps;
                if ((moduleProps = MODILES_PROPS.get(depend)) == null) {
                    MODILES_PROPS.put(depend, moduleProps = ResourceUtil.createProps());
                    if (!ResourceUtil.loadFormClasspath(moduleProps, depend + ".props")) {
                        throw buildException("Not found module: " + depend);
                    }
                    resolveModules(moduleProps.get(PROPS_MODULES));
                    if (MODULES.contains(depend)) {
                        continue;
                    }
                }
                MODULES.add(depend);
                props.merge(moduleProps);
            }
        }
    }

    public static Map<String, String> getFileTypeMap() {
        return fileTypeMap;
    }

    public static TreeSet<DependLib> getDepends() {
        return depends;
    }

    public static TreeSet<DependLib> getTestDepends() {
        return testDepends;
    }

    public static TreeSet<DependLib> getProvidedDepends() {
        return providedDepends;
    }

    public static String[] getCopys() {
        return getArrayWithoutComment("copys");
    }

    public static String[] getCreateFolders() {
        return getArrayWithoutComment("folders");
    }

    public static String[] getInitTemplates() {
        return getArrayWithoutComment("inits");
    }

    public static List<String> getCommonTemplates() {
        return commonTemplates;
    }

    public static List<String> getTableTemplates() {
        return tableTemplates;
    }

    public static String getWorkPath() {
        return workPath;
    }

    public static void setWorkPath(String workPath) {
        Config.workPath = workPath;
    }

    public static String getString(String key) {
        return configs.get(key);
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return CommonUtil.toBoolean(configs.get(key), defaultValue);
    }

    public static String getString(String key, String defaultValue) {
        final String value = configs.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static String getRequiredString(String key) {
        final String value = configs.get(key);
        if (value == null) {
            throw buildException("RequiredString not found:" + key);
        }
        return value;
    }

    public static String getStringBlankIfNull(String key) {
        return getString(key, "");
    }

    public static String[] getArray(String key) {
        return StringUtil.toArray(configs.get(key));
    }

    public static String[] getArrayWithoutComment(String key) {
        return StringUtil.toArrayExcludeCommit(configs.get(key));
    }

    public static Map<String, String> getMap(final String key) {
        final Map<String, String> result = new HashMap<>();
        final String prefix = key.concat(".");
        final int index = key.length() + 1;
        configs.entrySet().forEach((entry) -> {
            String property = entry.getKey();
            if (property.startsWith(prefix)) {
                result.put(property.substring(index), entry.getValue());
            }
        });
        return result;
    }

    private static RuntimeException buildException(String msg) {
        Logger.error("[GeneratorConfig]: " + msg);
        return new RuntimeException("[GeneratorConfig]: " + msg);
    }
}
