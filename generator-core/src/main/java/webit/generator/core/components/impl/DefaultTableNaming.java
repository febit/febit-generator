// Copyright (c) 2013, Webit Team. All Rights Reserved.

package webit.generator.core.components.impl;

import webit.generator.core.Config;
import webit.generator.core.components.TableNaming;
import webit.generator.core.util.ClassNameUtil;
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

    protected boolean toLower;

    public DefaultTableNaming() {
        this.toLower = Config.getBoolean("tableNamingToLower", true);
    }
    
    @Override
    public String entity(String sqlName) {
        if (this.toLower) {
            sqlName = sqlName.toLowerCase();
        }
        return ClassNameUtil.modelEntityNamingStrategy(sqlName);
    }

    @Override
    public String modelSimpleType(String entity) {
        return Config.getString("modelPrefix", "") + ClassNameUtil.upperFirst(entity) + Config.getString("modelSuffix", "");
    }

    @Override
    public String modelType(String modelSimpleType) {
        return Config.getString("modelPkg", Config.getRequiredString("basePkg")) + "." + modelSimpleType;
    }

    @Override
    public String remark(String remark) {
        return ClassNameUtil.fixRemark(remark);
    }
}
