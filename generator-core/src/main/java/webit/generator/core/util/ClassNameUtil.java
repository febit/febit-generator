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
public class ClassNameUtil {

    public static String getClassSimpleName(String classFullName) {
        return classFullName.substring(classFullName.lastIndexOf('.') + 1);
    }

    public static String getClassPackageName(String classFullName) {
        final int in = classFullName.lastIndexOf('.');
        if (in >= 0) {
            return classFullName.substring(0, in);
        }
        return "";
    }

    public static String baseNamingStrategy(String name) {
        //XXX: 使用 StringBuilder
        final String[] parts = StringUtil.splitc(StringUtil.cutPrefix(name, "_"), '_');
        //int i=1
        for (int i = 1,len = parts.length; i < len; i++) {
            parts[i] = upperFirst(parts[i]);
        }
        return lowerFirst(StringUtil.join(parts));
    }

    public static String modelEntityNamingStrategy(String tableSqlName) {
        return baseNamingStrategy(tableSqlName);
    }

    public static String modelColumnNamingStrategy(String columnSqlName) {
        return baseNamingStrategy(columnSqlName);
    }

    public static String upperFirst(String str) {
        if (str != null && str.length() != 0) {
            return CharUtil.toUpperAscii(str.charAt(0)) + str.substring(1);
        } else {
            return str;
        }
    }

    public static String lowerFirst(String str) {
        if (str != null && str.length() != 0) {
            return CharUtil.toLowerAscii(str.charAt(0)) + str.substring(1);
        } else {
            return str;
        }
    }

    public static String getGetterMethodName(String field, String javaType) {
        /*
         if (javaType.toLowerCase().endsWith("boolean")) {
              return "is" + upperFirst(field);
         }*/
        if (field.length() > 1
                && CharUtil.isUppercaseAlpha(field.charAt(1))) {
            return "get" + field;
        } else {
            return "get" + upperFirst(field);
        }
    }

    public static String getSetterMethodName(String field) {
        return "set" + upperFirst(field);
    }

    private static final String[] BASE_TYPES = {"double", "float", "byte", "short", "int", "long", "char", "boolean"};

    public static List javaImportsCheck(final List list, final String thisClassName) {
        final List<String> imports = new ArrayList<String>(list.size());
        final String thisPkg = ClassNameUtil.getClassPackageName(thisClassName);
        for (Iterator it = list.iterator(); it.hasNext();) {
            String item = (String) it.next();
            if (item == null) {
                continue;
            }
            while (item.endsWith("[]")) {
                item = item.substring(0, item.length() - 2);
            }
            final String pkg;
            if (imports.contains(item)
                    || (item.indexOf('.') < 0 && ArraysUtil.contains(BASE_TYPES, item))
                    || (pkg = ClassNameUtil.getClassPackageName(item)).equals("java.lang")
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
}
