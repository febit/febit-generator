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
package org.febit.generator.components;

import java.util.Map;
import org.febit.generator.Generator;
import org.febit.generator.model.Table;
import org.febit.lang.Singleton;

/**
 *
 * @author zqq90
 */
public interface GeneratorProcesser extends Singleton {

    void init(Generator generator);

    void afterInitRoot();

    void beforeMargeTemplates();

    void beforeMargeCommonTemplate(String templateName, Map<String, Object> params);

    void beforeMargeTableTemplate(String templateName, Map<String, Object> params, Table table);
}
