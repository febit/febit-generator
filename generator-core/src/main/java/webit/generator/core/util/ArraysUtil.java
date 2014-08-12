// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

/**
 *
 * @author zqq90
 */
public class ArraysUtil {

    public static final String[] EMPTY_STRINGS = new String[0];
    
    public static String[] subarray(String[] src, int offset, int len) {
        String dest[] = new String[len];
        System.arraycopy(src, offset, dest, 0, len);
        return dest;
    }

    public static boolean in(Object src, Object[] arr) {
        return ArraysUtil.contains(arr, src);
    }

    public static boolean contains(Object[] array, Object value) {
        return indexOf(array, value, 0) != -1;
    }

    public static int indexOf(Object[] array, Object value) {
        return indexOf(array, value, 0);
    }

    public static int indexOf(Object[] array, Object value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (value != null) {
                if (value.equals(array[i])) {
                    return i;
                }
            } else if (array[i] == null) {
                return i;
            }
        }
        return -1;
    }
}
