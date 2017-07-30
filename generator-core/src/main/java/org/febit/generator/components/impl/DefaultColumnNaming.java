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
package org.febit.generator.components.impl;

import org.febit.generator.Config;
import org.febit.generator.components.ColumnNaming;
import org.febit.generator.util.NamingUtil;

/**
 *
 * @author zqq90
 */
public class DefaultColumnNaming extends ColumnNaming {

    protected boolean toCamelCase;

    public DefaultColumnNaming() {
    }

    @Override
    public String sqlName(String sqlNameRaw) {
        return sqlNameRaw;
    }

    @Override
    public String varName(String sqlName) {
        if (this.toCamelCase) {
            return NamingUtil.toLowerCamelCase(sqlName);
        }
        return sqlName;
    }

    @Override
    public String getterName(String varName, String javaType) {
        return NamingUtil.getGetterName(varName, javaType);
    }

    @Override
    public String setterName(String varName, String javaType) {
        return NamingUtil.getSetterName(varName);
    }

    @Override
    public String remark(String remark) {
        return NamingUtil.fixRemark(remark);
    }
}
