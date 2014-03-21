// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.filesaver.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import webit.generator.core.filesaver.AbstractAppendFileSaver;
import webit.generator.core.util.FileUtil;
import webit.generator.core.util.Logger;
import webit.generator.core.util.TemplateContextUtil;

/**
 *
 * @author zqq90
 */
public class AppendFileSaver extends AbstractAppendFileSaver {

    protected String outroot;
    protected final Set<String> clearedFiles = new HashSet<String>();

    public void init(String outroot) {
        this.outroot = outroot;
    }

    protected String getFilePath(TemplateContextUtil.FileEntry fileEntry) {
        final String realpath = FileUtil.concat(outroot, fileEntry.fileName);
        if (clearedFiles.contains(realpath) == false) {
            clearedFiles.add(realpath);
            try {
                FileUtil.deleteFile(realpath);
            } catch (FileNotFoundException ignore) {
            } catch (IOException ex) {
                Logger.warn("try to delete file failed: " + realpath, ex);
            }
        }
        return realpath;
    }
}
