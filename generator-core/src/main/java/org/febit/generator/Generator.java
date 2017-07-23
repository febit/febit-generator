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
import java.util.List;
import java.util.Map;
import org.febit.generator.components.GeneratorProcesser;
import org.febit.generator.components.TableFactory;
import org.febit.generator.model.Table;
import org.febit.generator.saver.FileEntry;
import org.febit.generator.saver.FileSaver;
import org.febit.generator.saver.FolderEntry;
import org.febit.generator.util.FileUtil;
import org.febit.generator.util.Logger;
import org.febit.generator.util.ResourceUtil;
import org.febit.wit.Engine;
import org.febit.wit.exceptions.ParseException;
import org.febit.wit.global.GlobalManager;
import org.febit.wit.io.Out;
import org.febit.wit.io.impl.DiscardOut;
import org.febit.wit.lang.KeyValues;
import org.febit.wit.util.KeyValuesUtil;

/**
 *
 * @author zqq90
 */
public class Generator {

    private String outroot;
    private GlobalManager globalManager;
    private Engine templateEngine;
    private List<Table> tableList;
    private List<Table> whiteTables;
    private GeneratorProcesser[] processers;
    private Map<Object, FileSaver> fileSaverMap;

    private void initTemplateEngine() {
        final Engine engine = Engine.create(Config.getString("engine.props"));
        this.globalManager = engine.getGlobalManager();
        this.templateEngine = engine;
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
                            if (Logger.isDebugEnabled()) {
                                Logger.debug("Run init: " + templateName);
                            }
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

        final Map<String, Object> params = new HashMap<>();
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

        this.templateEngine.getTemplate(templateName).merge(params, Logger.out);

        for (FileEntry fileEntry : TemplateContext.popFiles()) {
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
        for (FolderEntry folderEntry : TemplateContext.popFolders()) {
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
    }

    protected void initFileSaver() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Map<Object, FileSaver> fileSavers = this.fileSaverMap = new HashMap<>();
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
            this.globalManager.setConst(typeName, i);
            ++i;
            Logger.info("Loaded FileType:" + typeName);
        }
    }

    protected FileSaver getFileSaver(Object fileType) {
        return fileSaverMap.get(fileType);
    }

    protected void initRoot() throws IOException {

        this.globalManager.setConst("DEBUG", Config.getBoolean("debug"));
        this.globalManager.setConst("basePkg", Config.getRequiredString("basePkg"));
        this.globalManager.setConst("modelPkg", Config.getRequiredString("modelPkg"));
        this.globalManager.setConst("modelPrefix", Config.getString("modelPrefix", ""));

        this.globalManager.setConst("db", Config.getMap("db"));

        this.globalManager.setConst("depends", Config.getDepends());
        this.globalManager.setConst("testDepends", Config.getTestDepends());
        this.globalManager.setConst("providedDepends", Config.getProvidedDepends());

        for (Map.Entry<String, String> entry : Config.getMap("extra").entrySet()) {
            this.globalManager.setConst(entry.getKey(), entry.getValue());
        }

        this.tableList = TableFactory.getTables();
        whiteTables = new ArrayList<>(this.tableList.size());
        for (Table table : tableList) {
            if (!table.isBlackEntity) {
                whiteTables.add(table);
            }
        }

        this.globalManager.setConst("tables", this.tableList);
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
