// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.core.model;

import java.util.Map;
import webit.generator.core.dbaccess.model.ColumnRaw;
import webit.generator.core.util.ResourceUtil;

/**
 *
 * @author Zqq
 */
public abstract class TableNaming {

    private static TableNaming _instance;

    public abstract String sqlName(String rawSqlName);
    
    public abstract String entity(String sqlName);
    
    public static TableNaming instance() {
        TableNaming instance = _instance;
        if (instance == null) {
            instance = _instance = (TableNaming) ResourceUtil.loadComponent("tableNaming");
        }
        return instance;
    }
}
