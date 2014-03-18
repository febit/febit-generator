// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.typeconverter.impl;

import webit.generator.core.typeconverter.Converter;

/**
 *
 * @author zqq90
 */
public class DoubleConverter implements Converter<Double> {

    public Double convert(String stringValue) {
        return Double.valueOf(stringValue);
    }
}
