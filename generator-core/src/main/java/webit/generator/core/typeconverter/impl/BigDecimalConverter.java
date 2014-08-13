package webit.generator.core.typeconverter.impl;

import java.math.BigDecimal;
import webit.generator.core.typeconverter.Converter;
import webit.generator.core.util.Logger;

/**
 *
 * @author Zqq
 */
public class BigDecimalConverter implements Converter<BigDecimal> {

    @Override
    public BigDecimal convert(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        stringValue = stringValue.trim();
        if (stringValue.length() == 0) {
            return null;
        }
        try {
            return new BigDecimal(stringValue);
        } catch (NumberFormatException e) {
            Logger.error("Format error for [" + getClass().getName() + "]： " + stringValue, e);
            return null;
        }
    }
}
