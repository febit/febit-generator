/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.generator.typeconverter;

import java.util.HashMap;
import java.util.Map;
import org.febit.generator.Config;
import org.febit.generator.typeconverter.impl.StringConverter;
import org.febit.generator.util.ClassLoaderUtil;
import org.febit.generator.util.Logger;

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
