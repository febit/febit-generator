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

import org.febit.generator.components.TableNaming;
import org.febit.generator.util.NamingUtil;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class DefaultTableNaming extends TableNaming {

    protected String tablePrefix;
    protected String modelPrefix;
    protected String modelSuffix;
    protected String modelPkg;
    protected boolean toCamelCase;

    @Override
    public String sqlName(String sqlNameRaw) {
        return StringUtil.cutPrefix(sqlNameRaw, tablePrefix);
    }

    @Override
    public String entity(String sqlName) {
        if (this.toCamelCase) {
            return NamingUtil.toLowerCamelCase(sqlName);
        }
        return sqlName;
    }

    protected String modelSimpleType(String entity) {
        return modelPrefix + NamingUtil.upperFirst(entity) + modelSuffix;
    }

    @Override
    public String modelType(String entity) {
        return modelPkg + "." + modelSimpleType(entity);
    }

    @Override
    public String remark(String remark) {
        return NamingUtil.fixRemark(remark);
    }
}
