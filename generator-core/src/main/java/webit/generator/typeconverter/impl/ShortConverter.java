// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.typeconverter.impl;

import webit.generator.typeconverter.Converter;
import webit.generator.util.Logger;

/**
 *
 * @author zqq90
 */
public class ShortConverter implements Converter<Short> {

    @Override
    public Short convert(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        stringValue = stringValue.trim();
        if (stringValue.length() == 0) {
            return null;
        }
        try {
            return Short.valueOf(stringValue);
        } catch (NumberFormatException e) {
            Logger.error("Format error for [" + getClass().getName() + "]： " + stringValue);
            return null;
        }
    }
}
