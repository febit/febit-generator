// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import webit.generator.core.dbaccess.DatabaseAccesser;
import webit.generator.core.dbaccess.model.Table;
import webit.generator.core.filesaver.FileSaver;
import webit.generator.core.model.TableModel;
import webit.generator.core.util.FileUtil;
import webit.generator.core.util.Logger;
import webit.generator.core.util.ResourceUtil;
import webit.generator.core.util.StringUtil;
import webit.generator.core.util.TemplateContextUtil;
import webit.script.Engine;
import webit.script.exceptions.ParseException;
import webit.script.global.GlobalManager;
import webit.script.io.Out;
import webit.script.io.impl.DiscardOut;
import webit.script.util.SimpleBag;
import webit.script.util.keyvalues.KeyValues;
import webit.script.util.keyvalues.KeyValuesUtil;

/**
 *
 * @author ZQQ
 */
public class Generator {

    public static final String RES_DEFAULT_AUTH = "default.actionauth";
    public static final String RES_TABLE_AUTH = "tableAuth";
    public static final String RES_TABLE_COLUMN = "tableColumn";
    private String outroot;
    private Map<Object, FileSaver> fileSaverMap;
    private SimpleBag constMap;
    private final Map<String, TableModel> tableMaps = new HashMap<String, TableModel>();
    private List<TableModel> whiteTables;
    private final Set<String> blackEntitys = new HashSet<String>();

    private Engine templateEngine;
    private GeneratorProcesser[] processers;

    private void initTemplateEngine() {
        final Engine engine;
        this.templateEngine = engine = Engine.createEngine(Config.getString("webit.script.props"));
        final GlobalManager globalManager = engine.getGlobalManager();
        this.constMap = globalManager.getConstBag();

        globalManager.getGlobalBag().set("currtable", null);
        globalManager.commit();
    }

    public void process() {
        try {

            outroot = FileUtil.concat(Config.getWorkPath(), Config.getRequiredString("outroot"));
            Logger.info("outroot: " + outroot);

            List<String> processersClass = StringUtil.toUnBlankList(Config.getString("processers"));
            if (processersClass != null && processersClass.size() != 0) {
                processers = new GeneratorProcesser[processersClass.size()];
                int i = 0;
                for (String string : processersClass) {
                    GeneratorProcesser processer = processers[i++] = (GeneratorProcesser) ResourceUtil.loadClass(string).newInstance();
                    processer.init(this);
                }
            } else {
                processers = new GeneratorProcesser[0];
            }

            initTemplateEngine();
            initRoot();
            for (GeneratorProcesser processer : processers) {
                processer.afterInitRoot();
            }
            initFileSaver();

            //create_folders
            {
                final List<String> folders = Config.getCreateFolders();
                if (folders != null) {
                    for (String folder : Config.getCreateFolders()) {
                        final int i = folder.indexOf('/');
                        getFileSaver(folder.substring(0, i)).createFolder(folder.substring(i + 1));
                    }
                }
            }

            //copys
            {
                final List<String> copys = Config.getCopys();
                if (copys != null) {
                    for (String copy : copys) {
                        final int i = copy.indexOf('/');
                        String fileSaverName = copy.substring(0, i);
                        FileSaver fileSaver = getFileSaver(fileSaverName);
                        if (fileSaver == null) {
                            Logger.error("Not found FileSaver:" + fileSaverName);
                            throw new RuntimeException("Not found FileSaver:" + fileSaverName);
                        }
                        fileSaver.copyFile(copy.substring(i + 1), copy);
                    }
                }
            }

            //init Templates
            {
                final List<String> initTemplates;
                if ((initTemplates = Config.getInitTemplates()) != null && initTemplates.size() > 0) {
                    final GlobalManager globalManager = this.templateEngine.getGlobalManager();
                    final Out out = new DiscardOut();
                    final KeyValues params = KeyValuesUtil.wrap(new String[]{
                        "GLOBAL",
                        "CONST"
                    }, new Object[]{
                        globalManager.getGlobalBag(),
                        globalManager.getConstBag()
                    });

                    for (String templateName : initTemplates) {
                        if (templateName != null
                                && (templateName = templateName.trim()).length() != 0) {
                            this.templateEngine.getTemplate(templateName)
                                    .merge(params, out);
                            //Commit Global
                            globalManager.commit();
                        }
                    }
                }

            }

            margeTemplates();
        } catch (Exception ex) {
            ex.printStackTrace(Logger.out);
        }
    }

    protected void margeTemplates() throws IOException, ParseException, Exception {

        for (GeneratorProcesser processer : processers) {
            processer.beforeMargeTemplates();
        }
        //prepare Engine
        final GlobalManager globalManager = this.templateEngine.getGlobalManager();
        globalManager.commit();

        final Map<String, Object> params = new HashMap<String, Object>();
        // Common Templates
        for (String item : Config.getCommonTemplates()) {
            params.clear();
            for (GeneratorProcesser processer : processers) {
                processer.beforeMargeCommonTemplate(item, params);
            }
            margeTemplate(item, params);
        }

        //reg 
        final int currtableGlobalIndex = globalManager.getGlobalIndex("currtable");
        // Table Templates
        final List<String> tableTemplates = Config.getTableTemplates();

        for (TableModel table : whiteTables) {
            globalManager.setGlobal(currtableGlobalIndex, table);
            for (String item : tableTemplates) {
                params.clear();
                for (GeneratorProcesser processer : processers) {
                    processer.beforeMargeTableTemplate(item, params, table);
                }
                margeTemplate(item, params);
            }
        }
    }

    protected void margeTemplate(String templateName, Map<String, Object> params) throws IOException, ParseException, Exception {

        TemplateContextUtil.reset();
        this.templateEngine.getTemplate(templateName).merge(params, Logger.out);

        for (TemplateContextUtil.FileEntry fileEntry : TemplateContextUtil.files) {
            if (fileEntry.cancel) {
                continue;
            }
            final FileSaver fileSaver = getFileSaver(fileEntry.type);
            if (fileSaver != null) {
                fileSaver.saveFile(templateName, fileEntry);
            } else {
                throw new Exception("TmplFileSaver not found with id: " + fileEntry.type);
            }
        }
        for (TemplateContextUtil.FolderEntry folderEntry : TemplateContextUtil.folders) {
            if (folderEntry.cancel) {
                continue;
            }
            final FileSaver fileSaver = getFileSaver(folderEntry.type);
            if (fileSaver != null) {
                fileSaver.createFolder(folderEntry.fileName);
            } else {
                throw new Exception("TmplFileSaver not found with id: " + folderEntry.type);
            }
        }
        TemplateContextUtil.reset();
    }

    protected void initFileSaver() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Map<Object, FileSaver> fileSavers = this.fileSaverMap = new HashMap<Object, FileSaver>();
        Map<String, String> fileTypeMap = Config.getFileTypeMap();
        int i = 0;
        for (Map.Entry<String, String> entry : fileTypeMap.entrySet()) {
            String typeName = entry.getKey();
            String className = entry.getValue();

            Class typeClass = ResourceUtil.loadClass(className);
            FileSaver tmplFileSaver = (FileSaver) typeClass.newInstance();
            tmplFileSaver.init(outroot);

            fileSavers.put(typeName, tmplFileSaver);
            fileSavers.put(i, tmplFileSaver);
            constMap.set(typeName, i);
            ++i;
            Logger.info("Loaded FileType:" + typeName);
        }
    }

    protected FileSaver getFileSaver(Object fileType) {
        return fileSaverMap.get(fileType);
    }

    protected void initRoot() throws IOException {

        //加载表黑名单
        {
            final List<String> tableBlacks = StringUtil.toUnBlankList(Config.getString("blackEntitys"));
            if (tableBlacks != null) {
                blackEntitys.addAll(tableBlacks);
            }
        }

        constMap.set("DEBUG", Config.getBoolean("debug"));
        constMap.set("basePkg", Config.getRequiredString("basePkg"));
        constMap.set("modelPkg", Config.getRequiredString("modelPkg"));
        constMap.set("modelPrefix", Config.getString("modelPrefix", ""));

        constMap.set("db", Config.getMap("db"));

        constMap.set("depends", Config.getDepends());
        constMap.set("testDepends", Config.getTestDepends());
        constMap.set("providedDepends", Config.getProvidedDepends());

        for (Map.Entry<String, String> entry : Config.getMap("extra").entrySet()) {
            constMap.set(entry.getKey(), entry.getValue());
        }

        final Map<String, Map<String, Map<String, Object>>> tableColumnMap = ResourceUtil.loadTableColumns();
        for (Map.Entry<String, Table> entry : DatabaseAccesser.getInstance().getAllTables().entrySet()) {
            //String string = entry.getKey();
            Table table = entry.getValue();
            tableMaps.put(table.name, new TableModel(table, tableColumnMap.get(table.name)));
        }

        //tables init
        for (Map.Entry<String, TableModel> entry : tableMaps.entrySet()) {
            entry.getValue().init(tableMaps);
        }

        //MAP_TO_LIST and without black tables
        final List<TableModel> tableList = this.whiteTables = new ArrayList<TableModel>(tableMaps.size());
        TableModel tableModel;
        for (Map.Entry<String, TableModel> entry : tableMaps.entrySet()) {
            tableModel = entry.getValue();
            if (!blackEntitys.contains(tableModel.getEntity())) {
                tableList.add(tableModel);
            }
        }

        //table list sort
        Collections.sort(tableList);
        if (Logger.isInfoEnabled()) {
            for (TableModel tableModel1 : tableList) {
                Logger.info("Loaded table: " + tableModel1.getSqlName() + "  " + tableModel1.getRemarks());
            }
        }
        constMap.set("tables", tableList);
    }

    public String getOutroot() {
        return outroot;
    }

    public Map<Object, FileSaver> getFileSaverMap() {
        return fileSaverMap;
    }

    public Map<String, TableModel> getTableMaps() {
        return tableMaps;
    }

    public List<TableModel> getWhiteTables() {
        return whiteTables;
    }

    public Set<String> getBlackEntitys() {
        return blackEntitys;
    }

    public Engine getTemplateEngine() {
        return templateEngine;
    }
}
