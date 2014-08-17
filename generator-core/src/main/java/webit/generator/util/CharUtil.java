// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.util;

/**
 *
 * @author zqq90
 */
public class CharUtil {

    public static boolean equalsOne(final char c, final char[] match) {
        for (int i = 0, len = match.length; i < len; i++) {
            if (c == match[i]) {
                return true;
            }
        }
        return false;
    }

    public static int findFirstDiff(char[] source, int index, char match) {
        for (int i = index; i < source.length; i++) {
            if (source[i] != match) {
                return i;
            }
        }
        return -1;
    }

    public static int findFirstDiff(char[] source, int index, char[] match) {
        for (int i = index; i < source.length; i++) {
            if (equalsOne(source[i], match) == false) {
                return i;
            }
        }
        return -1;
    }

    public static int findFirstEqual(char[] source, int index, char match) {
        for (int i = index; i < source.length; i++) {
            if (source[i] == match) {
                return i;
            }
        }
        return -1;
    }

    public static int findFirstEqual(char[] source, int index, char[] match) {
        for (int i = index; i < source.length; i++) {
            if (equalsOne(source[i], match)) {
                return i;
            }
        }
        return -1;
    }

    public static char toUpperAscii(char c) {
        if (isLowercaseAlpha(c)) {
            c -= (char) 0x20;
        }
        return c;
    }

    public static char toLowerAscii(char c) {
        if ((c >= 'A') && (c <= 'Z')) {
            c += (char) 0x20;
        }
        return c;
    }

    public static boolean isUppercaseAlpha(char c) {
        return (c >= 'A') && (c <= 'Z');
    }

    public static boolean isLowercaseAlpha(char c) {
        return (c >= 'a') && (c <= 'z');
    }

    public static boolean isWhitespace(char c) {
        return c <= ' ';
    }
}
