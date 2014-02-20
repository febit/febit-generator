// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.filesaver.impl;

import webit.generator.core.filesaver.AbstractFileSaver;
import webit.generator.core.util.FileUtil;
import webit.generator.core.util.TemplateContextUtil;

/**
 *
 * @author Zqq
 */
public class ResourceFileFileSaver extends AbstractFileSaver {

    private static final String RES_ROOT = "src/main/resources";
    protected String basePath;

    @Override
    protected String getBasePath() {
        return basePath;
    }

    @Override
    protected String getFilePath() {
        return  TemplateContextUtil.getFileName();
    }

    public void init(String outroot) {
        basePath = FileUtil.concat(outroot, RES_ROOT);
    }
}
