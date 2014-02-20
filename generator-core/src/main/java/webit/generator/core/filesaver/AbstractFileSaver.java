// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.filesaver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import webit.generator.core.util.FileUtil;
import webit.generator.core.util.Logger;
import webit.generator.core.util.StreamUtil;
import webit.generator.core.util.TemplateContextUtil;
import webit.script.util.ClassLoaderUtil;

/**
 *
 * @author Zqq
 */
public abstract class AbstractFileSaver implements FileSaver {

    private final static String COPY_ROOT = "copy/";

    protected abstract String getFilePath();
    protected abstract String getBasePath();

    protected byte[] getContentFromTmpl() {
        Object object = TemplateContextUtil.getContent();
        if (object instanceof byte[]) {
            return (byte[]) object;
        }
        return String.valueOf(object).getBytes();
    }

    public boolean copyFile(String file, String src) {

        String from = FileUtil.concatWithUnixStyle(COPY_ROOT, src);

        String to = FileUtil.concat(getBasePath(), file);

        InputStream input = ClassLoaderUtil.getDefaultClassLoader().getResourceAsStream(from);
        if (input == null) {
            Logger.error("Not found resource: " + from);
            return false;
        }
        try {
            FileUtil.mkdirs4File(to);
            FileOutputStream output = new FileOutputStream(new File(to));
            try {
                StreamUtil.copy(input, output);
            } catch (IOException ex) {
                return dealExceptions("Unable to copyFile: " + to, ex);
            } finally {
                StreamUtil.close(output);
            }
        } catch (FileNotFoundException ex) {
            return dealExceptions("Unable to copyFile: " + to, ex);
        } catch (IOException ex) {
            return dealExceptions("Unable to create folder for: " + to, ex);
        } finally {
            StreamUtil.close(input);
        }
        return true;
    }

    public boolean createFolder(String folder) {
        String dir = FileUtil.concat(getBasePath(), folder);
        try {
            FileUtil.mkdirs(dir);
        } catch (IOException ex) {
            return dealExceptions("Unable to creatFolder: " + dir, ex);
        }
        return true;
    }

    public boolean saveFile(String tmpl) {
        String path = getFilePath();
        try {
            writeFile(path, getContentFromTmpl());
        } catch (IOException ex) {
            return dealExceptions("Unable to saveFile: " + path, ex);
        }
        return true;
    }

    protected void writeFile(String path, byte[] code) throws IOException {
        FileUtil.writeFile(getBasePath(), path, code);
    }

    protected boolean dealExceptions(String msg, Exception ex) {

        Logger.error(msg, ex);
        return false;
    }
}
