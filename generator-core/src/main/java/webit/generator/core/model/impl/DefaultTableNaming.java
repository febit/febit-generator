// Copyright (c) 2013, Webit Team. All Rights Reserved.

package webit.generator.core.model.impl;

import webit.generator.core.Config;
import webit.generator.core.model.TableNaming;
import webit.generator.core.util.ClassNameUtil;
import webit.generator.core.util.StringUtil;

/**
 *
 * @author Zqq
 */
public class DefaultTableNaming extends TableNaming{

    @Override
    public String sqlName(String rawSqlName) {
        return StringUtil.cutPrefix(rawSqlName, Config.getString("db.tablePrefix", ""));
    }

    @Override
    public String entity(String sqlName) {
        return ClassNameUtil.modelEntityNamingStrategy(sqlName);
    }
}
