// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.typeconverter.impl;

import webit.generator.core.typeconverter.Converter;

/**
 *
 * @author zqq90
 */
public class LongConverter implements Converter<Long> {

    public Long convert(String stringValue) {
        return Long.valueOf(stringValue);
    }
}
