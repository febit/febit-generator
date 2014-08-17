// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.typeconverter.impl;

import webit.generator.typeconverter.Converter;
import webit.generator.util.Logger;

/**
 *
 * @author zqq90
 */
public class TimeMillisConverter implements Converter<Long> {

    @Override
    public Long convert(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        stringValue = stringValue.trim();
        if (stringValue.length() == 0) {
            return null;
        }
        //TODO： 识别标准Date 格式
        try {
            return Long.valueOf(stringValue);
        } catch (NumberFormatException e) {
            Logger.error("Format error for [" + getClass().getName() + "]： " + stringValue);
            return null;
        }
    }
}
