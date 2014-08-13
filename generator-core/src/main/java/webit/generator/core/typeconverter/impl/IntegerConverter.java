// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.typeconverter.impl;

import webit.generator.core.typeconverter.Converter;
import webit.generator.core.util.Logger;

/**
 *
 * @author zqq90
 */
public class IntegerConverter implements Converter<Integer> {

    @Override
    public Integer convert(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        stringValue = stringValue.trim();
        if (stringValue.length() == 0) {
            return null;
        }
        try {
            return Integer.valueOf(stringValue);
        } catch (NumberFormatException e) {
            Logger.error("Format error for [" + getClass().getName() + "]： " + stringValue);
            return null;
        }
    }
}
