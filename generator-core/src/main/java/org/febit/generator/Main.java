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
import java.text.SimpleDateFormat;
import java.util.Date;
import org.febit.generator.model.DependLib;
import org.febit.generator.model.DependLibs;
import org.febit.generator.util.FileUtil;
import org.febit.generator.util.Logger;
import org.febit.util.Props;

/**
 *
 * @author zqq90
 */
public class Main {

    private static final String DEFAULT_PROPS = "generator-default.props";

    static {
        DependLib.noop();
        DependLibs.noop();
    }

    public static void generate(String fileFullPath) throws IOException {
        loadConfig(fileFullPath, "gen");
        Lazy.get(Generator.class).process();
    }

    public static void initConfig(String fileFullPath) throws IOException {
        loadConfig(fileFullPath, "init");
        Lazy.get(ConfigInit.class).process();
    }

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("Arguments count must be 2 at least.");
        }
        String action = args[0];
        if (action == null) {
            System.out.println("First argument must be 'gen' or 'init'.");
            return;
        }
        try {
            switch (action) {
                case "gen":
                    generate(args[1]);
                    break;
                case "init":
                    Main.initConfig(args[1]);
                    break;
                default:
                    System.out.println("First argument must be 'gen' or 'init'.");
                    break;
            }
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }
    }

    /*
     * 加载配置
     */
    private static void loadConfig(String configFile, String action) throws IOException {
        final String workPath = FileUtil.getPath(configFile);
        Props props = Props.shadowLoader()
                .load(DEFAULT_PROPS)
                .load("file:" + configFile)
                .get();
        props.set("workPath", workPath);

        Lazy.init(props);
        Config config = Lazy.config();
        for (String module : props.getModules()) {
            Config.addModule(module);
        }
        Logger.setLevel(config.get("logger.level"));
        Date now = new Date();
        Logger.setLogFile(FileUtil.concat(workPath, FileUtil.getName(configFile) + '.' + action + '.' + new SimpleDateFormat("yyyyMMddHHmmss").format(now) + ".log"));
        Logger.info("===================");
        Logger.info("TIME:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now));
        Logger.info("");
        Logger.info(" modules: " + props.getModulesString());
    }
}
