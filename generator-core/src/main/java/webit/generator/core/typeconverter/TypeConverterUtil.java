// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.typeconverter;

import java.util.HashMap;
import java.util.Map;
import webit.generator.core.Config;
import webit.generator.core.typeconverter.impl.StringConverter;
import webit.generator.core.util.Logger;
import webit.script.util.ClassLoaderUtil;

/**
 *
 * @author zqq90
 */
public class TypeConverterUtil {

    private static final Map<Object, Converter> CONVERTERS = new HashMap<Object, Converter>();
    private static final Converter DEFAULT_CONVERTER = new StringConverter();

    private static Converter resolveConverter(String type) {
        Converter converter = CONVERTERS.get(type);
        if (converter == null) {
            String convertType = Config.getString("typeConverter." + type);
            if (convertType != null) {
                try {
                    converter = (Converter) ClassLoaderUtil.getDefaultClassLoader().loadClass(convertType).newInstance();
                } catch (Exception ex) {
                    Logger.error("Unable to load Converter: " + type, ex);
                    throw new RuntimeException(ex);
                }
            } else {
                converter = DEFAULT_CONVERTER;
            }
            CONVERTERS.put(type, converter);
        }
        return converter;
    }

    public static Object convert(String type, String value) {
        return resolveConverter(type).convert(value);
    }
}
