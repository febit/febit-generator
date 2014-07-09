// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.core.model.impl;

import webit.generator.core.model.ColumnNaming;
import webit.generator.core.util.ClassNameUtil;

/**
 *
 * @author Zqq
 */
public class DefaultColumnNaming extends ColumnNaming {

    @Override
    public String sqlName(String rawSqlName) {
        return rawSqlName;
    }

    @Override
    public String varName(String sqlName) {
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

}
