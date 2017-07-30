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
import org.febit.util.ClassUtil;

/**
 *
 * @author zqq90
 */
public class TypeConverter {

    private static final Converter DEFAULT_CONVERTER = new StringConverter();

    protected Config config;
    protected final Map<Object, Converter> CONVERTERS = new HashMap<>();

    public Object convert(String type, String value) {
        return resolveConverter(type).convert(value);
    }

    protected Converter resolveConverter(String type) {
        Converter converter = CONVERTERS.get(type);
        if (converter == null) {
            String convertType = config.get("typeConverter." + type);
            if (convertType != null) {
                converter = (Converter) ClassUtil.newInstance(convertType);
            } else {
                converter = DEFAULT_CONVERTER;
            }
            CONVERTERS.put(type, converter);
        }
        return converter;
    }
}
