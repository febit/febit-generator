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

import java.util.Collection;

/**
 *
 * @author zqq90
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

        boolean each(V value);
    }

    public static <V> boolean each(Collection<V> collection, Handler<V> handler) {
        for (V item : collection) {
            if (!handler.each(item)) {
                return false;
            }
        }
        return true;
    }
}
