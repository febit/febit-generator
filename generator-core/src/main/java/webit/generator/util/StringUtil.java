// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zqq90
 */
public class StringUtil {

    public static final String[] EMPTY_ARRAY = org.febit.wit.util.ArrayUtil.EMPTY_STRINGS;
    public static final String EMPTY = "";

    public static String[] toArrayWithoutCommit(String src) {
        final String[] array = toArray(src);
        int count = 0;
        for (String str : array) {
            if (str.charAt(0) != '#') {
                array[count++] = str;
            }
        }
        if (count == 0) {
            return EMPTY_ARRAY;
        }
        if (count == array.length) {
            return array;
        }
        final String[] result = new String[count];
        System.arraycopy(array, 0, result, 0, count);
        return result;
    }

    public static String[] toArray(final String src) {
        return toArray(src, ',');
    }

    public static String[] toArray(final String src, final char split) {
        if (src == null || src.length() == 0) {
            return EMPTY_ARRAY;
        }

        final char[] srcc = src.toCharArray();
        final int len = srcc.length;

        // list max size = (size + 1) / 2
        List<String> list = new ArrayList<String>(
                len > 1024 ? 128
                        : len > 64 ? 32
                                : (len + 1) >> 1);

        int i = 0;
        while (i < len) {
            //skip empty & splits
            while (i < len) {
                char c = srcc[i];
                if (c != split
                        && c != '\n'
                        && c != '\r'
                        && c != ' '
                        && c != '\t') {
                    break;
                }
                i++;
            }
            //check if end
            if (i == len) {
                break;
            }
            final int start = i;

            //find end
            while (i < len) {
                char c = srcc[i];
                if (c == split
                        || c == '\n'
                        || c == '\r') {
                    break;
                }
                i++;
            }
            int end = i;
            //trim back end
            for (;;) {
                char c = srcc[end - 1];
                if (c == ' '
                        || c == '\t') {
                    end--;
                    continue;
                }
                break;
            }
            list.add(new String(srcc, start, end - start));
        }
        if (list.isEmpty()) {
            return EMPTY_ARRAY;
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] splitc(final String src, final String delimiters) {
        return splitc(src, delimiters.toCharArray());
    }

    public static String[] splitc(final String src, final char[] delimiters) {
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

    public static String join(String... parts) {
        final StringBuilder sb = new StringBuilder(parts.length * 16);
        for (String part : parts) {
            sb.append(part);
        }
        return sb.toString();
    }

    public static String join(Iterable elements, String separator) {
        if (elements == null) {
            return EMPTY;
        }
        final StringBuilder sb = new StringBuilder();
        for (Object o : elements) {
            if (sb.length() != 0
                    && separator != null) {
                sb.append(separator);
            }
            sb.append(o);
        }
        return sb.toString();
    }

    public static boolean isEmpty(String src) {
        return src == null || src.length() == 0;
    }

    public static boolean notEmpty(String src) {
        return src != null && src.length() != 0;
    }
}
