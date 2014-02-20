// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.typeconverter.impl;

import webit.generator.core.typeconverter.Converter;

/**
 *
 * @author zqq90
 */
public class TimeMillisConverter implements Converter<Long> {

    public Long convert(String stringValue) {
        //TODO： 识别标准Date 格式
        return Long.parseLong(stringValue);
    }
}
