// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ZQQ
 */
public class NamingUtil {

    public static String getClassSimpleName(String className) {
        final int index = className.lastIndexOf('.');
        if (index >= 0) {
            return className.substring(index + 1);
        }
        return className;
    }

    public static String getClassPackageName(String classFullName) {
        final int index = classFullName.lastIndexOf('.');
        if (index > 0) {
            return classFullName.substring(0, index);
        }
        return "";
    }

    public static String baseNamingStrategy(String name) {
        //XXX: 使用 StringBuilder
        final String[] parts = StringUtil.splitc(StringUtil.cutPrefix(name, "_"), '_');
        for (int i = 1, len = parts.length; i < len; i++) {
            parts[i] = upperFirst(parts[i]);
        }
        return lowerFirst(StringUtil.join(parts));
    }

    public static String upperFirst(String str) {
        if (str != null
                && str.length() != 0
                && CharUtil.isLowercaseAlpha(str.charAt(0))) {
            char[] chars = str.toCharArray();
            chars[0] = CharUtil.toUpperAscii(chars[0]);
            return new String(chars);
        } else {
            return str;
        }
    }

    public static String lowerFirst(String str) {
        if (str != null
                && str.length() != 0
                && CharUtil.isUppercaseAlpha(str.charAt(0))) {
            char[] chars = str.toCharArray();
            chars[0] = CharUtil.toLowerAscii(chars[0]);
            return new String(chars);
        } else {
            return str;
        }
    }

    public static String getGetterMethodName(String field, String javaType) {
        if (field.length() <= 1
                || CharUtil.isLowercaseAlpha(field.charAt(1))) {
            field = upperFirst(field);
        }
        return "get".concat(field);
    }

    public static String getSetterMethodName(String field) {
        if (field.length() <= 1
                || CharUtil.isLowercaseAlpha(field.charAt(1))) {
            field = upperFirst(field);
        }
        return "set".concat(field);
    }

    private static final String[] BASE_TYPES = {"double", "float", "byte", "short", "int", "long", "char", "boolean"};

    public static List javaImportsCheck(final List list, final String thisClassName) {
        final List<String> imports = new ArrayList<String>(list.size());
        final String thisPkg = NamingUtil.getClassPackageName(thisClassName);
        for (Iterator it = list.iterator(); it.hasNext();) {
            String item = (String) it.next();
            if (item == null) {
                continue;
            }
            while (item.endsWith("[]")) {
                item = item.substring(0, item.length() - 2);
            }
            if (imports.contains(item)
                    || (item.indexOf('.') < 0 && Arrays.contains(BASE_TYPES, item))) {
                continue;
            }
            final String pkg = NamingUtil.getClassPackageName(item);
            if ("java.lang".equals(pkg)
                    || pkg.equals(thisPkg)
                    || imports.contains(pkg.concat(".*"))) {
                continue;
            }
            imports.add(item);
        }
        Collections.sort(imports);
        return imports;
    }

    public static String varNameToUpper(String str) {
        final int size;
        final char[] chars;
        final StringBuilder sb = new StringBuilder((size = (chars = str.toCharArray()).length) * 3 / 2 + 1);
        char c;
        for (int i = 0; i < size; i++) {
            c = chars[i];
            if (CharUtil.isLowercaseAlpha(c)) {
                sb.append(CharUtil.toUpperAscii(c));
            } else {
                sb.append('_').append(c);
            }
        }
        return sb.toString();
    }

    public static String fixRemark(String remark) {
        if (remark == null) {
            return null;
        }
        int splitIndex = remark.lastIndexOf("//");
        if (splitIndex >= 0) {
            remark = remark.substring(0, splitIndex);
        }
        return remark.trim();
    }

}
