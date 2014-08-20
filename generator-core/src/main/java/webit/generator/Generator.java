// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webit.generator.components.GeneratorProcesser;
import webit.generator.components.TableFactory;
import webit.generator.model.Table;
import webit.generator.saver.FileEntry;
import webit.generator.saver.FileSaver;
import webit.generator.saver.FolderEntry;
import webit.generator.util.FileUtil;
import webit.generator.util.Logger;
import webit.generator.util.ResourceUtil;
import webit.script.Engine;
import webit.script.exceptions.ParseException;
import webit.script.global.GlobalManager;
import webit.script.io.Out;
import webit.script.io.impl.DiscardOut;
import webit.script.lang.Bag;
import webit.script.lang.KeyValues;
import webit.script.util.KeyValuesUtil;

/**
 *
 * @author ZQQ
 */
public class Generator {
    
    private String outroot;
    private Bag constMap;
    private Engine templateEngine;
    private List<Table> tableList;
    private List<Table> whiteTables;
    private GeneratorProcesser[] processers;
    private Map<Object, FileSaver> fileSaverMap;

    private void initTemplateEngine() {
        final Engine engine = Engine.createEngine(Config.getString("engine.props"));
        final GlobalManager globalManager = engine.getGlobalManager();
        
        globalManager.commit();
        
        this.templateEngine = engine;
        this.constMap = globalManager.getConstBag();
    }

    public void process() {
        try {

            outroot = FileUtil.concat(Config.getWorkPath(), Config.getRequiredString("outroot"));
            Logger.info("outroot: " + outroot);

            String[] processersClass = Config.getArrayWithoutComment("processers");
            processers = new GeneratorProcesser[processersClass.length];
            {
                int i = 0;
                for (String string : processersClass) {
                    Logger.info("Running processer: " + string);
                    GeneratorProcesser processer = processers[i++] = (GeneratorProcesser) ResourceUtil.loadClass(string).newInstance();
                    processer.init(this);
                }
            }

            initTemplateEngine();
            initRoot();
            for (GeneratorProcesser processer : processers) {
                processer.afterInitRoot();
            }
            initFileSaver();

            //create_folders
            {
                for (String folder : Config.getCreateFolders()) {
                    final int i = folder.indexOf('/');
                    getFileSaver(folder.substring(0, i)).createFolder(folder.substring(i + 1));
                }
            }

            //copys
            {
                for (String copy : Config.getCopys()) {
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

            //init Templates
            {
                final String[] initTemplates = Config.getInitTemplates();
                if (initTemplates.length != 0) {
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
            //TODO: log table summary
            margeTemplates();
        } catch (Exception ex) {
            Logger.error(ex.getMessage(), ex);
        }
        //TODO: log summary
    }

    protected void margeTemplates() throws IOException, ParseException, Exception {

        for (GeneratorProcesser processer : processers) {
            processer.beforeMargeTemplates();
        }
        //prepare Engine
        final GlobalManager globalManager = this.templateEngine.getGlobalManager();
        globalManager.getGlobalBag().set("currtable", null);
        globalManager.commit();
        final int currtableGlobalIndex = globalManager.getGlobalIndex("currtable");

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
        // Table Templates
        final List<String> tableTemplates = Config.getTableTemplates();
        for (Table table : whiteTables) {
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

        TemplateContext.reset();
        this.templateEngine.getTemplate(templateName).merge(params, Logger.out);

        for (FileEntry fileEntry : TemplateContext.files) {
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
        for (FolderEntry folderEntry : TemplateContext.folders) {
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
        TemplateContext.reset();
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

        this.tableList = TableFactory.getTables();
        whiteTables = new ArrayList<Table>(this.tableList.size());
        for (Table table : tableList) {
            if (!table.getIsBlackEntity()) {
                whiteTables.add(table);
            }
        }

        constMap.set("tables", this.tableList);
    }

    public String getOutroot() {
        return outroot;
    }

    public Map<Object, FileSaver> getFileSaverMap() {
        return fileSaverMap;
    }

    public List<Table> getWhiteTables() {
        return whiteTables;
    }

    public List<Table> getTableList() {
        return tableList;
    }

    public Engine getTemplateEngine() {
        return templateEngine;
    }
}
