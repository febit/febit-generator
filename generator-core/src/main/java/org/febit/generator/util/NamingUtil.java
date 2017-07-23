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
package org.febit.generator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.febit.util.CharUtil;

/**
 *
 * @author zqq90
 */
public class NamingUtil {

    public static String getClassSimpleName(String className) {
        final int index = className.lastIndexOf('.');
        if (index >= 0) {
            return className.substring(index + 1);
        }
        return className;
    }

    public static String getClassPackageName(String className) {
        final int index = className.lastIndexOf('.');
        if (index > 0) {
            return className.substring(0, index);
        }
        return "";
    }

    public static String toLowerCamelCase(String name) {
        char[] buffer = name.toCharArray();
        int count = 0;
        boolean lastUnderscore = false;
        for (int i = 0; i < buffer.length; i++) {
            char c = buffer[i];
            if (c == '_') {
                lastUnderscore = true;
            } else {
                c = (lastUnderscore && count != 0)
                        ? CharUtil.toUpperAscii(c)
                        : CharUtil.toLowerAscii(c);
                buffer[count++] = c;
                lastUnderscore = false;
            }
        }
        if (count != buffer.length) {
            buffer = Arrays.subarray(buffer, 0, count);
        }
        return new String(buffer);
    }

    public static String upperFirst(String str) {
        if (str != null
                && str.length() != 0
                && CharUtil.isLowercaseAlpha(str.charAt(0))) {
            final char[] chars = str.toCharArray();
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
            final char[] chars = str.toCharArray();
            chars[0] = CharUtil.toLowerAscii(chars[0]);
            return new String(chars);
        } else {
            return str;
        }
    }

    public static String getGetterName(String field, String javaType) {
        if (field.length() <= 1
                || CharUtil.isLowercaseAlpha(field.charAt(1))) {
            field = upperFirst(field);
        }
        return "get".concat(field);
    }

    public static String getSetterName(String field) {
        if (field.length() <= 1
                || CharUtil.isLowercaseAlpha(field.charAt(1))) {
            field = upperFirst(field);
        }
        return "set".concat(field);
    }

    private static final String[] BASE_TYPES = {"double", "float", "byte", "short", "int", "long", "char", "boolean"};

    public static List javaImportsCheck(final List list, final String thisClassName) {
        final List<String> imports = new ArrayList<>(list.size());
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
