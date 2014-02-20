// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

/**
 *
 * @author zqq90
 */
public class ArraysUtil {

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
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }
}
