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
import org.febit.generator.model.DependLibs;
import org.febit.generator.model.Table;
import org.febit.generator.saver.FileEntry;
import org.febit.generator.saver.FileSaver;
import org.febit.generator.saver.FolderEntry;
import org.febit.generator.util.Logger;
import org.febit.lang.Singleton;
import org.febit.util.CollectionUtil;
import org.febit.util.Petite;
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
public class Generator implements Singleton {

    protected Config config;
    protected Petite petite;

    protected DependLibs depends;
    protected DependLibs testDepends;
    protected DependLibs providedDepends;

    protected String[] tmpls;
    protected String[] copys;
    protected String[] folders;
    protected String[] inits;

    protected String outroot;
    protected GeneratorProcesser[] processers;
    protected TableFactory tableFactory;

    protected String engineProps;
    protected boolean debug;
    protected String basePkg;  //Required
    protected String modelPkg; //Required
    protected String modelPrefix;

    private Engine templateEngine;
    private List<Table> tableList;
    private List<Table> whiteTables;

    private final Map<Object, FileSaver> fileSavers = new HashMap<>();
    private final List<String> commonTemplates = new ArrayList<>();
    private final List<String> tableTemplates = new ArrayList<>();

    @Petite.Init
    public void init() {
        this.templateEngine = Engine.create(engineProps);
        for (String item : tmpls) {
            if (item.startsWith("#")) {
                continue;
            }
            if (item.endsWith(".each")) {
                tableTemplates.add(item);
            } else {
                commonTemplates.add(item);
            }
        }

        Logger.info("Common Templates amount: " + commonTemplates.size());
        Logger.info("Table  Templates amount: " + tableTemplates.size());
    }

    protected void initFileSaver() {
        GlobalManager globalManager = this.templateEngine.getGlobalManager();

        int i = 0;
        for (Map.Entry<String, String> entry : config.extract("filetypes.").entrySet()) {
            String key = entry.getKey();
            String className = entry.getValue();

            FileSaver tmplFileSaver = (FileSaver) petite.get(className);
            tmplFileSaver.init(outroot);

            fileSavers.put(key, tmplFileSaver);
            fileSavers.put(i, tmplFileSaver);
            globalManager.setConst(key, i);
            i++;
            Logger.info("Loaded FileType:" + key);
        }
    }

    public void process() {
        try {
            _process();
        } catch (Exception ex) {
            Logger.error(ex.getMessage(), ex);
        }
    }

    protected void _process() throws IOException, Exception {
        Logger.info("outroot: " + outroot);
        initRoot();
        for (GeneratorProcesser processer : processers) {
            processer.afterInitRoot();
        }
        initFileSaver();

        //create_folders
        for (String folder : folders) {
            if (folder.startsWith("#")) {
                continue;
            }
            final int i = folder.indexOf('/');
            getFileSaver(folder.substring(0, i)).createFolder(folder.substring(i + 1));
        }

        //copys
        for (String copy : copys) {
            if (copy.startsWith("#")) {
                continue;
            }
            final int i = copy.indexOf('/');
            String fileSaverName = copy.substring(0, i);
            FileSaver fileSaver = getFileSaver(fileSaverName);
            if (fileSaver == null) {
                Logger.error("Not found FileSaver:" + fileSaverName);
                throw new RuntimeException("Not found FileSaver:" + fileSaverName);
            }
            fileSaver.copyFile(copy.substring(i + 1), copy);
        }

        //init Templates
        if (inits.length != 0) {
            final GlobalManager globalManager = this.templateEngine.getGlobalManager();
            final Out out = new DiscardOut();
            final KeyValues params = KeyValuesUtil.wrap(new String[]{
                "GLOBAL",
                "CONST"
            }, new Object[]{
                globalManager.getGlobalBag(),
                globalManager.getConstBag()
            });

            for (String templateName : inits) {
                if (templateName.startsWith("#")) {
                    continue;
                }
                if (Logger.isDebugEnabled()) {
                    Logger.debug("Run init: " + templateName);
                }
                this.templateEngine.getTemplate(templateName)
                        .merge(params, out);
                //Commit Global
                globalManager.commit();
            }
        }
        //TODO: log table summary
        margeTemplates();
        //TODO: log summary
    }

    protected void margeTemplates() throws IOException, ParseException {

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
        for (String item : commonTemplates) {
            params.clear();
            for (GeneratorProcesser processer : processers) {
                processer.beforeMargeCommonTemplate(item, params);
            }
            margeTemplate(item, params);
        }

        //reg 
        // Table Templates
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

    protected void margeTemplate(String templateName, Map<String, Object> params) throws IOException, ParseException {

        this.templateEngine.getTemplate(templateName).merge(params, Logger.out);

        for (FileEntry fileEntry : TemplateContext.popFiles()) {
            if (fileEntry.cancel) {
                continue;
            }
            final FileSaver fileSaver = getFileSaver(fileEntry.type);
            if (fileSaver != null) {
                fileSaver.saveFile(templateName, fileEntry);
            } else {
                throw new RuntimeException("TmplFileSaver not found with id: " + fileEntry.type);
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
                throw new RuntimeException("TmplFileSaver not found with id: " + folderEntry.type);
            }
        }
    }

    protected FileSaver getFileSaver(Object fileType) {
        return fileSavers.get(fileType);
    }

    protected void initRoot() throws IOException {

        GlobalManager globalManager = this.templateEngine.getGlobalManager();
        globalManager.setConst("DEBUG", debug);
        globalManager.setConst("basePkg", basePkg);
        globalManager.setConst("modelPkg", modelPkg);
        globalManager.setConst("modelPrefix", modelPrefix);

        globalManager.setConst("db", config.extract("db."));

        globalManager.setConst("depends", depends);
        globalManager.setConst("testDepends", testDepends);
        globalManager.setConst("providedDepends", providedDepends);

        for (Map.Entry<String, String> entry : config.extract("extra.").entrySet()) {
            globalManager.setConst(entry.getKey(), entry.getValue());
        }

        this.tableList = tableFactory.getTables();
        this.whiteTables = CollectionUtil.toIter(this.tableList)
                .filter((Table table) -> !table.isBlackEntity)
                .readList();

        globalManager.setConst("tables", this.tableList);
    }

    public String getOutroot() {
        return outroot;
    }

    public Map<Object, FileSaver> getFileSaverMap() {
        return fileSavers;
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
