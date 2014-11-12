// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.util;

/**
 *
 * @author zqq
 */
public class CommonUtil {

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
