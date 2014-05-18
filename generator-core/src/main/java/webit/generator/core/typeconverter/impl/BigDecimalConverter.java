package webit.generator.core.typeconverter.impl;

import java.math.BigDecimal;
import webit.generator.core.typeconverter.Converter;
import webit.generator.core.util.Logger;

/**
 *
 * @author Zqq
 */
public class BigDecimalConverter implements Converter<BigDecimal> {

    public BigDecimal convert(String stringValue) {
        if (stringValue != null && (stringValue = stringValue.trim()).length() != 0) {
            try {
                return new BigDecimal(stringValue);
            } catch (Exception e) {
                Logger.error("默认值的格式错误：" + stringValue, e);
                return null;
            }
        } else {
            return null;
        }
    }
}
