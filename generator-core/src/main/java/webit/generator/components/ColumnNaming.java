// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.components;

import webit.generator.util.ResourceUtil;

/**
 *
 * @author Zqq
 */
public abstract class ColumnNaming {

    private static ColumnNaming _instance;

    public abstract String remark(String remark);
    
    public abstract String sqlName(String rawSqlName);
    
    public abstract String varName(String sqlName);

    public abstract String getterName(String name, String javaType);

    public abstract String setterName(String name, String javaType);

    public static ColumnNaming instance() {
        ColumnNaming instance = _instance;
        if (instance == null) {
            instance = _instance = (ColumnNaming) ResourceUtil.loadComponent("columnNaming");
        }
        return instance;
    }
}
