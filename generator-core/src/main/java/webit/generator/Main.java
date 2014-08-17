// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import webit.generator.util.FileUtil;
import webit.generator.util.Logger;
import webit.generator.util.StringUtil;

/**
 *
 * @author ZQQ
 */
public class Main {

    protected static void initGenerator(String configFile, String action) throws IOException {
        final String workpath = FileUtil.getPath(configFile);

        Config.load(configFile);
        Logger.setLevel(Config.getString("logger.level"));
        
        Date now = new Date();
        Logger.setLogFile(FileUtil.concat(workpath, FileUtil.getName(configFile) + '.' + action + '.' + new SimpleDateFormat("yyyMMddhhmmss").format(now) + ".log"));
        Logger.info("===================");
        Logger.info("TIME:" + new SimpleDateFormat("yyy-MM-dd hh:mm:ss").format(now));
        Logger.info("");

        Config.setWorkPath(workpath);

        Logger.info("Active Models: " + StringUtil.join(Config.MODULES, ", "));
        Logger.info("Common Templates amount: " + Config.getCommonTemplates().size());
        Logger.info("Table  Templates amount: " + Config.getTableTemplates().size());
    }
    
    public static void generate(String fileFullPath) throws IOException {
        initGenerator(fileFullPath, "gen");
        new Generator().process();
    }

    public static void initConfig(String fileFullPath) throws IOException {
        initGenerator(fileFullPath, "init");
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
                initConfig(args[1]);
            } else {
                System.out.println("First argument must be 'gen' or 'init'.");
            }
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }
    }

    @Deprecated
    public static void generator(String fileFullPath) throws IOException {
        generate(fileFullPath);
    }
}
