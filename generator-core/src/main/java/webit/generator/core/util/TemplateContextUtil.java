// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zqq90
 */
public class TemplateContextUtil {

    public final static Map<String, Object> backMap = new HashMap<String, Object>();

    public final static String FILE_TYPE = "FILE_TYPE";
    public final static String FILE_NAME = "FILE_NAME";
    public final static String CANCEL = "CANCEL";
    public final static String CONTENT = "CONTENT";

    public static void put(String key, Object value) {
        backMap.put(key, value);
    }

    public static Object get(String key) {
        return backMap.get(key);
    }

    public static void reset() {
        backMap.clear();
    }

    public static int getFileType() {
        final Object value;
        return (value = get(FILE_TYPE)) != null ? (Integer) value : 0;
    }

    public static boolean isCancel() {
        final Object value;
        return (value = get(CANCEL)) != null ? (Boolean) value : false;
    }

    public static String getFileName() {
        return String.valueOf(get(FILE_NAME));
    }

    public static Object getContent() {
        return get(CONTENT);
    }
}
