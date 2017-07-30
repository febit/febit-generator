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
package org.febit.generator;

import java.util.HashSet;
import java.util.Set;
import org.febit.util.BaseConfig;
import org.febit.util.StringUtil;

public class Config extends BaseConfig<Config> {

    protected static final Set<String> MODULES = new HashSet<>();

    public static void addModule(String module) {
        if (module == null) {
            return;
        }
        MODULES.add(module);
        module = StringUtil.cutPrefix(module, "classpath:");
        MODULES.add(module);
        module = StringUtil.cutSuffix(module, ".props");
        MODULES.add(module);
    }

    public static boolean isModuleActived(String module) {
        return MODULES.contains(module);
    }
}
