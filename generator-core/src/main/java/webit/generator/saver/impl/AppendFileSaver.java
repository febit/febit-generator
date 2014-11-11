// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.saver.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import webit.generator.saver.AbstractAppendFileSaver;
import webit.generator.saver.FileEntry;
import webit.generator.util.FileUtil;
import webit.generator.util.Logger;

/**
 *
 * @author zqq90
 */
public class AppendFileSaver extends AbstractAppendFileSaver {

    protected String outroot;
    protected final Set<String> clearedFiles = new HashSet<String>();

    @Override
    public void init(String outroot) {
        this.outroot = outroot;
    }

    @Override
    protected String getFilePath(FileEntry fileEntry) {
        final String realpath = FileUtil.concat(outroot, fileEntry.fileName);
        if (!clearedFiles.contains(realpath)) {
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
