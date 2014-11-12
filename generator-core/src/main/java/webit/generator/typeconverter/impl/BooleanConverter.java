// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.typeconverter.impl;

import webit.generator.typeconverter.Converter;
import webit.generator.util.CommonUtil;

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
        stringValue = stringValue.trim();
        if (stringValue.isEmpty()) {
            return null;
        }
        return CommonUtil.toBoolean(stringValue);
    }
}
