// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.core.components;

import webit.generator.core.util.ResourceUtil;

/**
 *
 * @author Zqq
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
