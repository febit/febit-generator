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
import org.febit.generator.util.FileUtil;
import org.febit.generator.util.Logger;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class Main {

    public static void generate(String fileFullPath) throws IOException {
        loadConfig(fileFullPath, "gen");
        new Generator().process();
    }

    public static void initConfig(String fileFullPath) throws IOException {
        loadConfig(fileFullPath, "init");
        new ConfigInit().process();
    }

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("Arguments count must be 2 at least.");
        }
        String action = args[0];
        try {
            if ("gen".equals(action)) {
                generate(args[1]);
            } else if ("init".equals(action)) {
                Main.initConfig(args[1]);
            } else {
                System.out.println("First argument must be 'gen' or 'init'.");
            }
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }
    }

    /*
     * 加载配置
     */
    private static void loadConfig(String configFile, String action) throws IOException {
        final String workpath = FileUtil.getPath(configFile);

        Config.load(configFile);
        Logger.setLevel(Config.getString("logger.level"));

        Date now = new Date();
        Logger.setLogFile(FileUtil.concat(workpath, FileUtil.getName(configFile) + '.' + action + '.' + new SimpleDateFormat("yyyyMMddHHmmss").format(now) + ".log"));
        Logger.info("===================");
        Logger.info("TIME:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now));
        Logger.info("");

        Config.setWorkPath(workpath);

        Logger.info("Active Models: " + StringUtil.join(Config.MODULES, ", "));
        Logger.info("Common Templates amount: " + Config.getCommonTemplates().size());
        Logger.info("Table  Templates amount: " + Config.getTableTemplates().size());
    }
}
