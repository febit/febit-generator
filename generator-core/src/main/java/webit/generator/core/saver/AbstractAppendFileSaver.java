// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.saver;

import java.io.IOException;
import webit.generator.core.util.FileUtil;
import webit.generator.core.util.Logger;

/**
 *
 * @author Zqq
 */
public abstract class AbstractAppendFileSaver implements FileSaver {

    protected abstract String getFilePath(FileEntry fileEntry);

    protected byte[] getContentFromTmpl(FileEntry fileEntry) {
        final Object object = fileEntry.context;
        if (object instanceof byte[]) {
            return (byte[]) object;
        }
        return String.valueOf(object).getBytes();
    }

    @Override
    public boolean copyFile(String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean createFolder(String folder) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean saveFile(String tmpl, FileEntry fileEntry) {
        final String realpath = getFilePath(fileEntry);
        try {
            FileUtil.appendBytes(realpath, getContentFromTmpl(fileEntry));
        } catch (IOException ex) {
            Logger.error("Unable to saveFile: " + realpath, ex);
            return false;
        }
        return true;
    }
}
