// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Logger {

    public static final int TRACE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;
    private static int _level = DEBUG;
    public static PrintStream out = System.out;
    public static PrintStream err = System.err;

    public static void setLogFile(String filepath) throws IOException {
        File file = new File(filepath);
        FileUtil.mkdirs4File(filepath);
        try {
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (file.exists()) {
            err = out = new PrintStream(file, "UTF-8");
        }
    }

    public static void setLevel(String level) {
        if (level != null) {
            level = level.trim().toLowerCase();
            if ("error".equals(level)) {
                _level = ERROR;
                return;
            } else if ("warn".equals(level)) {
                _level = WARN;
                return;
            } else if ("info".equals(level)) {
                _level = INFO;
                return;
            } else if ("debug".equals(level)) {
                _level = DEBUG;
                return;
            } else if ("trace".equals(level)) {
                _level = TRACE;
                return;
            }
        }
        //default
        _level = DEBUG;
    }

    public static void trace(String s) {
        if (_level <= TRACE) {
            out.println("[TRACE] " + s);
        }
    }

    public static void debug(String s) {
        if (_level <= DEBUG) {
            out.println("[DEBUG] " + s);
        }
    }

    public static void info(String s) {
        if (_level <= INFO) {
            out.println("[INFO ] " + s);
        }
    }

    public static void warn(String s) {
        if (_level <= WARN) {
            err.println("[WARN ] " + s);
        }
    }

    public static void warn(String s, Throwable e) {
        if (_level <= WARN) {
            err.println("[WARN ] " + s + " cause:" + e);
            e.printStackTrace(err);
        }
    }

    public static void error(String s) {
        if (_level <= ERROR) {
            err.println("[ERROR] " + s);
        }
    }

    public static void error(String s, Throwable e) {
        if (_level <= ERROR) {
            err.println("[ERROR] " + s + " cause:" + e);
            e.printStackTrace(err);
        }
    }

    public static boolean isTraceEnabled() {
        return _level <= TRACE;
    }

    public static boolean isDebugEnabled() {
        return _level <= DEBUG;
    }

    public static boolean isInfoEnabled() {
        return _level <= INFO;
    }

    public static boolean isWarnEnabled() {
        return _level <= WARN;
    }

    public static boolean isErrorEnabled() {
        return _level <= ERROR;
    }
}
