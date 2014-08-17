package webit.generator.typeconverter.impl;

import java.math.BigInteger;
import webit.generator.typeconverter.Converter;
import webit.generator.util.Logger;

/**
 *
 * @author Zqq
 */
public class BigIntegerConverter implements Converter<BigInteger> {

    @Override
    public BigInteger convert(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        stringValue = stringValue.trim();
        if (stringValue.length() == 0) {
            return null;
        }
        try {
            return new BigInteger(stringValue);
        } catch (NumberFormatException e) {
            Logger.error("Format error for [" + getClass().getName() + "]： " + stringValue, e);
            return null;
        }
    }
}
