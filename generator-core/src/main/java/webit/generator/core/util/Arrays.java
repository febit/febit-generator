package webit.generator.core.util;

import java.util.Collection;

/**
 *
 * @author Zqq
 */
public class Arrays {

    public static final String[] EMPTY_STRINGS = new String[0];

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

    public static int indexOf(Object[] array, Object value) {
        return indexOf(array, value, 0);
    }

    public static boolean contains(Object[] array, Object value) {
        return indexOf(array, value, 0) != -1;
    }

    public static String[] subarray(String[] src, int offset, int len) {
        String[] dest = new String[len];
        System.arraycopy(src, offset, dest, 0, len);
        return dest;
    }

    public static char[] subarray(char[] src, int offset, int len) {
        char[] dest = new char[len];
        System.arraycopy(src, offset, dest, 0, len);
        return dest;
    }

    public static boolean in(Object src, Object[] arr) {
        return contains(arr, src);
    }

    public static interface Handler<V> {

        boolean each(int index, V value);
    }

    public static <V> boolean each(Collection<V> collection, Handler<V> handler) {
        int index = 0;
        for (V item : collection) {
            if (handler.each(index++, item) == false) {
                return false;
            }
        }
        return true;
    }
}
