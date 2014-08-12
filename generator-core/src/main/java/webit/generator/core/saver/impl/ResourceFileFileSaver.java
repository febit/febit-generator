// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.saver.impl;

import webit.generator.core.saver.AbstractFileSaver;
import webit.generator.core.saver.FileEntry;
import webit.generator.core.util.FileUtil;

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
    protected String getFilePath(FileEntry fileEntry) {
        return fileEntry.fileName;
    }

    @Override
    public void init(String outroot) {
        basePath = FileUtil.concat(outroot, RES_ROOT);
    }
}
