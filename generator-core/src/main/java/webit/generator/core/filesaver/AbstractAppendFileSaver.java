// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.filesaver;

import java.io.IOException;
import webit.generator.core.util.FileUtil;
import webit.generator.core.util.Logger;
import webit.generator.core.util.TemplateContextUtil;

/**
 *
 * @author Zqq
 */
public abstract class AbstractAppendFileSaver implements FileSaver {

    protected abstract String getFilePath();

    protected byte[] getContentFromTmpl() {
        final Object object = TemplateContextUtil.getContent();
        if (object instanceof byte[]) {
            return (byte[]) object;
        }
        return String.valueOf(object).getBytes();
    }

    public boolean copyFile(String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean createFolder(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean saveFile(String string) {
        final String realpath = getFilePath();
        try {
            FileUtil.appendBytes(realpath, getContentFromTmpl());
        } catch (IOException ex) {
            Logger.error("Unable to saveFile: " + realpath, ex);
            return false;
        }
        return true;
    }
}
