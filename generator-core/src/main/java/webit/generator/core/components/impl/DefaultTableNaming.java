// Copyright (c) 2013, Webit Team. All Rights Reserved.

package webit.generator.core.components.impl;

import webit.generator.core.Config;
import webit.generator.core.components.TableNaming;
import webit.generator.core.util.NamingUtil;
import webit.generator.core.util.StringUtil;

/**
 *
 * @author Zqq
 */
public class DefaultTableNaming extends TableNaming{

    @Override
    public String sqlName(String sqlNameRaw) {
        return StringUtil.cutPrefix(sqlNameRaw, Config.getString("db.tablePrefix", ""));
    }

    protected boolean toCamelCase;

    public DefaultTableNaming() {
        this.toCamelCase = Config.getBoolean("tableNaming.toCamelCase", true);
    }
    
    @Override
    public String entity(String sqlName) {
        if (this.toCamelCase) {
            return NamingUtil.toLowerCamelCase(sqlName);
        }
        return sqlName;
    }

    @Override
    public String modelSimpleType(String entity) {
        return Config.getString("modelPrefix", "") + NamingUtil.upperFirst(entity) + Config.getString("modelSuffix", "");
    }

    @Override
    public String modelType(String modelSimpleType) {
        return Config.getString("modelPkg", Config.getRequiredString("basePkg")) + "." + modelSimpleType;
    }

    @Override
    public String remark(String remark) {
        return NamingUtil.fixRemark(remark);
    }
}
