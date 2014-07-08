// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zqq90
 */
public class StringUtil {

    private final static char[] DELIMITERS = ",\n\r".toCharArray();

    public static String[] toTrimedArray(String string) {
        if (string != null) {
            final String[] array;
            trimAll(array = splitc(string, DELIMITERS));
            return array;
        }
        return null;
    }

    //XXX: 可优化
    public static List<String> toUnBlankList(String string) {
        final String[] array;
        array = toTrimedArray(string);
        if (array != null) {
            final int size;
            final List<String> list = new ArrayList<String>(size = array.length);
            String item;
            for (int i = 0; i < size; i++) {
                if ((item = array[i]).length() != 0) {
                    list.add(item);
                }
            }
            return list;
        } else {
            return null;
        }
    }

    public static String replace(String s, String sub, String with) {
        int c = 0;
        int i = s.indexOf(sub, c);
        if (i == -1) {
            return s;
        }
        int length = s.length();
        StringBuilder sb = new StringBuilder(length + with.length());
        do {
            sb.append(s.substring(c, i));
            sb.append(with);
            c = i + sub.length();
        } while ((i = s.indexOf(sub, c)) != -1);
        if (c < length) {
            sb.append(s.substring(c, length));
        }
        return sb.toString();
    }

    public static String cutPrefix(String string, String prefix) {
        if (string.startsWith(prefix)) {
            string = string.substring(prefix.length());
        }
        return string;
    }

    public static String cutSuffix(String string, String suffix) {
        if (string.endsWith(suffix)) {
            string = string.substring(0, string.length() - suffix.length());
        }
        return string;
    }

    public static final String EMPTY = "";

    public static void trimAll(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (string != null) {
                strings[i] = string.trim();
            }
        }
    }

    public static String[] splitc(String src, char delimiter) {
        if (src.length() == 0) {
            return new String[]{EMPTY};
        }
        char[] srcc = src.toCharArray();

        int maxparts = srcc.length + 1;
        int[] start = new int[maxparts];
        int[] end = new int[maxparts];

        int count = 0;

        start[0] = 0;
        int s = 0, e;
        if (srcc[0] == delimiter) {	// string starts with delimiter
            end[0] = 0;
            count++;
            s = CharUtil.findFirstDiff(srcc, 1, delimiter);
            if (s == -1) {							// nothing after delimiters
                return new String[]{"", ""};
            }
            start[1] = s;							// new start
        }
        while (true) {
            // find new end
            e = CharUtil.findFirstEqual(srcc, s, delimiter);
            if (e == -1) {
                end[count] = srcc.length;
                break;
            }
            end[count] = e;

            // find new start
            count++;
            s = CharUtil.findFirstDiff(srcc, e, delimiter);
            if (s == -1) {
                start[count] = end[count] = srcc.length;
                break;
            }
            start[count] = s;
        }
        count++;
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = src.substring(start[i], end[i]);
        }
        return result;
    }

    public static String[] splitc(String src, String delimiters) {
        return splitc(src, delimiters.toCharArray());
    }

    public static String[] splitc(String src, char[] delimiters) {
        if ((delimiters.length == 0) || (src.length() == 0)) {
            return new String[]{src};
        }
        char[] srcc = src.toCharArray();

        int maxparts = srcc.length + 1;
        int[] start = new int[maxparts];
        int[] end = new int[maxparts];

        int count = 0;

        start[0] = 0;
        int s = 0, e;
        if (CharUtil.equalsOne(srcc[0], delimiters)) {	// string starts with delimiter
            end[0] = 0;
            count++;
            s = CharUtil.findFirstDiff(srcc, 1, delimiters);
            if (s == -1) {							// nothing after delimiters
                return new String[]{"", ""};
            }
            start[1] = s;							// new start
        }
        while (true) {
            // find new end
            e = CharUtil.findFirstEqual(srcc, s, delimiters);
            if (e == -1) {
                end[count] = srcc.length;
                break;
            }
            end[count] = e;

            // find new start
            count++;
            s = CharUtil.findFirstDiff(srcc, e, delimiters);
            if (s == -1) {
                start[count] = end[count] = srcc.length;
                break;
            }
            start[count] = s;
        }
        count++;
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = src.substring(start[i], end[i]);
        }
        return result;
    }

    public static String join(String... parts) {
        StringBuilder sb = new StringBuilder(parts.length);
        for (String part : parts) {
            sb.append(part);
        }
        return sb.toString();
    }

    public static String join(Iterable elements, String separator) {
        if (elements == null) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (Object o : elements) {
            if (sb.length() != 0
                    && separator != null) {
                sb.append(separator);
            }
            sb.append(o);
        }
        return sb.toString();
    }
}
