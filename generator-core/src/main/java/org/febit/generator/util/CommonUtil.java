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

import java.util.List;

/**
 *
 * @author zqq90
 */
public class CommonUtil {

    public static List adds(List list, Object[] objs) {
        if (objs != null) {
            list.addAll(java.util.Arrays.asList(objs));
        }
        return list;
    }

    public static boolean toBoolean(final Object value) {
        return toBoolean(value, false);
    }

    public static boolean toBoolean(final Object value, final boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        final String str = value.toString().trim();
        return str.equals("1")
                || str.equalsIgnoreCase("true")
                || str.equalsIgnoreCase("on")
                || str.equals("b'1'") //bit(1) false: b'0', true: b'1'
                ;
    }
}
