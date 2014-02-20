// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.filesaver.impl;

import webit.generator.core.filesaver.AbstractFileSaver;
import webit.generator.core.util.FileUtil;
import webit.generator.core.util.TemplateContextUtil;

/**
 *
 * @author Zqq
 */
public class JavaFileSaver extends AbstractFileSaver {

    private static final String JAVA_SRC_ROOT = "src/main/java";
    protected String basePath;

    @Override
    protected String getBasePath() {
        return basePath;
    }

    @Override
    protected String getFilePath() {
        String classname =  TemplateContextUtil.getFileName();
        return classname.replace('.', FileUtil.SYSTEM_SEPARATOR) + ".java";
    }

    public void init(String outroot) {
        basePath = FileUtil.concat(outroot, JAVA_SRC_ROOT);
    }
}