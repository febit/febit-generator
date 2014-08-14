// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.core.components.impl;

import webit.generator.core.Config;
import webit.generator.core.components.ColumnNaming;
import webit.generator.core.util.NamingUtil;

/**
 *
 * @author Zqq
 */
public class DefaultColumnNaming extends ColumnNaming {

    protected boolean toCamelCase;

    public DefaultColumnNaming() {
        this.toCamelCase = Config.getBoolean("columnNaming.toCamelCase", true);
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
