// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.typeconverter.impl;

import webit.generator.core.typeconverter.Converter;

/**
 *
 * @author zqq90
 */
public class BooleanConverter implements Converter<Boolean> {

    @Override
    public Boolean convert(String stringValue) {
        stringValue = stringValue.toLowerCase();
        return stringValue.equals("true")
                || stringValue.equals("1")
                || stringValue.equals("on");
    }
}
