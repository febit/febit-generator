// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.typeconverter.impl;

import webit.generator.core.typeconverter.Converter;

/**
 *
 * @author zqq90
 */
public class ShortConverter implements Converter<Short> {

    public Short convert(String stringValue) {
        return Short.valueOf(stringValue);
    }
}
