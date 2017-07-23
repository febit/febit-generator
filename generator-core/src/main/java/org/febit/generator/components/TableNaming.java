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

import org.febit.generator.util.ResourceUtil;

/**
 *
 * @author zqq90
 */
public abstract class TableNaming {

    private static TableNaming _instance;

    public abstract String remark(String remark);
    
    public abstract String sqlName(String sqlNameRaw);
    
    public abstract String entity(String sqlName);
    
    public abstract String modelSimpleType(String entity);
    
    public abstract String modelType(String modelSimpleType);
    
    public static TableNaming instance() {
        TableNaming instance = _instance;
        if (instance == null) {
            instance = _instance = (TableNaming) ResourceUtil.loadComponent("tableNaming");
        }
        return instance;
    }
}
