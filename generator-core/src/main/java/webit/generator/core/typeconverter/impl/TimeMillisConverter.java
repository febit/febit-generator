// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.typeconverter.impl;

import webit.generator.core.typeconverter.Converter;
import webit.generator.core.util.Logger;

/**
 *
 * @author zqq90
 */
public class TimeMillisConverter implements Converter<Long> {

    @Override
    public Long convert(String stringValue) {
        //TODO： 识别标准Date 格式
        if (stringValue != null && (stringValue = stringValue.trim()).length() != 0) {
            try {
                return Long.valueOf(stringValue);
            } catch (NumberFormatException e) {
                Logger.error("默认值的格式错误：" + stringValue, e);
                return null;
            }
        } else {
            return null;
        }
    }
}
