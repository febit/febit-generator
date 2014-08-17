// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.typeconverter.impl;

import webit.generator.typeconverter.Converter;

/**
 *
 * @author zqq90
 */
public class BooleanConverter implements Converter<Boolean> {

    @Override
    public Boolean convert(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        stringValue = stringValue.trim().toLowerCase();
        if (stringValue.length() == 0) {
            return null;
        }
        return stringValue.equals("1")
                || stringValue.equals("b'1'") //bit(1) false: b'0', true: b'1'
                || stringValue.equals("true")
                || stringValue.equals("on");
    }
}
