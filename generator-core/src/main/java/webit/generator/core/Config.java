// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import webit.generator.core.model.DependLib;
import webit.generator.core.model.Table;
import webit.generator.core.util.Logger;
import webit.generator.core.util.PropsUtil;
import webit.generator.core.util.ResourceUtil;
import webit.generator.core.util.StringUtil;
import webit.script.core.Tokens;
import webit.script.util.props.Props;

public class Config {

    private static Props props;
    private static final Map<String, String> configs = new HashMap<String, String>();
    private static String workPath;
    private static final String DEFAULT_PROPS_FILE = "generator-default.props";
    private static final String PROPS_MODULES = "modules";
    public static final ArrayList<String> MODULES = new ArrayList<String>();

    final static List<String> commonTemplates = new ArrayList<String>();
    final static List<String> tableTemplates = new ArrayList<String>();
    final static Map<String, String> fileTypeMap = new HashMap<String, String>();
    final static TreeSet<DependLib> depends = new TreeSet<DependLib>();
    final static TreeSet<DependLib> testDepends = new TreeSet<DependLib>();
    final static TreeSet<DependLib> providedDepends = new TreeSet<DependLib>();

    private static boolean loadedDefault = false;

    private static void initalize() {
        configs.clear();
        props.extractProps(configs);
        //fileTypes
        {
            fileTypeMap.clear();
            final List<String> filetypes = StringUtil.toUnBlankList(configs.get("filetypes"));
            if (filetypes != null) {
                for (String item : filetypes) {
                    int index = item.indexOf('=');
                    if (index > 0) {
                        fileTypeMap.put(item.substring(0, index).trim(), item.substring(index + 1).trim());
                    } else {
                        throw buildException("filetypes lost '=',must be 'key = value");
                    }
                }
            }
        }

        //tmpls
        {
            tableTemplates.clear();
            commonTemplates.clear();
            final List<String> tmpls = StringUtil.toUnBlankList(configs.get("tmpls"));
            if (tmpls != null) {
                for (String item : tmpls) {
                    if (item.endsWith(".each")) {
                        tableTemplates.add(item);
                    } else {
                        commonTemplates.add(item);
                    }
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
        final List<String> dependList = StringUtil.toUnBlankList(configs.get(propsName));
        if (dependList != null) {
            for (String item : dependList) {
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
    }

    private static void loadDefault() {
        if (loadedDefault == false) {
            loadedDefault = true;
            MODULES.clear();
            props = PropsUtil.createFromClasspath(DEFAULT_PROPS_FILE);
            resolveModules("core");
        }
    }

    public static void load(String filename) {
        loadDefault();
        if (filename != null) {
            try {
                Props moduleProps = PropsUtil.createProps();
                PropsUtil.loadFormFile(moduleProps, filename);
                resolveModules(moduleProps.getValue(PROPS_MODULES));
                props.merge(moduleProps);
            } catch (IOException ex) {
                throw buildException("PROPS file not found:" + filename);
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

    public static Map<String, Map<String, Object>> getTableSettings(String entity) {
        return getTablesSettings().get(entity);
    }

    public static Map<String, Object> getColumnSettings(Table table, String varName) {
        return getColumnSettings(table.entity, varName);
    }

    public static Map<String, Object> getColumnSettings(String entity, String varName) {
        Map<String, Map<String, Object>> tableSettings = getTableSettings(entity);
        if (tableSettings != null) {
            return tableSettings.get(varName);
        }
        return null;
    }

    public static boolean isModuleActived(String name) {
        return MODULES.contains(name);
    }

    private final static Map<String, Props> MODILES_PROPS = new HashMap<String, Props>();

    private static void resolveModules(final String names) {
        if (names != null) {
            for (String depend : StringUtil.toUnBlankList(names)) {
                if (MODULES.contains(depend)) {
                    continue;
                }
                Props moduleProps;
                if ((moduleProps = MODILES_PROPS.get(depend)) == null) {
                    MODILES_PROPS.put(depend, moduleProps = PropsUtil.createProps());
                    if (PropsUtil.loadFormClasspath(moduleProps, depend + ".props") == false) {
                        throw buildException("Not found module:" + depend);
                    }
                    resolveModules(moduleProps.getValue(PROPS_MODULES));
                    if (MODULES.contains(depend)) {
                        continue;
                    }
                }
                MODULES.add(depend);
                props.merge(moduleProps);
            }
        }
    }

    public static List<String> getCommonTemplates() {
        return commonTemplates;
    }

    public static List<String> getTableTemplates() {
        return tableTemplates;
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

    public static List<String> getCopys() {
        return StringUtil.toUnBlankList(configs.get("copys"));
    }

    public static List<String> getCreateFolders() {
        return StringUtil.toUnBlankList(configs.get("folders"));
    }

    public static List<String> getInitTemplates() {
        return StringUtil.toUnBlankList(configs.get("inits"));
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
        final String value = configs.get(key);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value.trim());
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

    public static Map<String, String> getMap(final String key) {
        Map<String, String> result = new HashMap<String, String>();
        final String prefix = key.concat(".");
        final int index = key.length() + 1;
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String property = entry.getKey();
            if (property.startsWith(prefix)) {
                result.put(property.substring(index), entry.getValue());
            }
        }
        return result;
    }

    private static RuntimeException buildException(String msg) {
        Logger.error("[GeneratorConfig]: " + msg);
        return new RuntimeException("[GeneratorConfig]: " + msg);
    }
}
