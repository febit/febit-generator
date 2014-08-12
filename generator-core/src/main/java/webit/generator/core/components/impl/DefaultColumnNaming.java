// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.core.components.impl;

import webit.generator.core.Config;
import webit.generator.core.components.ColumnNaming;
import webit.generator.core.util.ClassNameUtil;

/**
 *
 * @author Zqq
 */
public class DefaultColumnNaming extends ColumnNaming {

    protected boolean sqlNameToLower;

    public DefaultColumnNaming() {
        this.sqlNameToLower = Config.getBoolean("columnNaming.sqlNameToLower", true);
    }
    
    @Override
    public String sqlName(String sqlNameRaw) {
        return sqlNameRaw;
    }

    @Override
    public String varName(String sqlName) {
        if (this.sqlNameToLower) {
            sqlName = sqlName.toLowerCase();
        }
        return ClassNameUtil.modelColumnNamingStrategy(sqlName);
    }

    @Override
    public String getterName(String varName, String javaType) {
        return ClassNameUtil.getGetterMethodName(varName, javaType);
    }

    @Override
    public String setterName(String varName, String javaType) {
        return ClassNameUtil.getSetterMethodName(varName);
    }

    @Override
    public String remark(String remark) {
        return ClassNameUtil.fixRemark(remark);
    }
}
