/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.generator.saver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import jodd.io.StreamUtil;
import org.febit.generator.util.FileUtil;
import org.febit.generator.util.Logger;
import org.febit.util.ClassUtil;

/**
 *
 * @author zqq90
 */
public abstract class AbstractFileSaver implements FileSaver {

    private final static String COPY_ROOT = "copy/";

    protected abstract String getFilePath(FileEntry fileEntry);
    protected abstract String getBasePath();

    protected byte[] getContentFromTmpl(FileEntry fileEntry) {
        final Object object = fileEntry.context;
        if (object instanceof byte[]) {
            return (byte[]) object;
        }
        return String.valueOf(object).getBytes();
    }

    @Override
    public boolean copyFile(String file, String src) {
        String from = FileUtil.concatWithUnixStyle(COPY_ROOT, src);
        String to = FileUtil.concat(getBasePath(), file);
        InputStream input = ClassUtil.getDefaultClassLoader().getResourceAsStream(from);
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

    @Override
    public boolean createFolder(String folder) {
        String dir = FileUtil.concat(getBasePath(), folder);
        try {
            FileUtil.mkdirs(dir);
        } catch (IOException ex) {
            return dealExceptions("Unable to creatFolder: " + dir, ex);
        }
        return true;
    }

    @Override
    public boolean saveFile(String tmpl, FileEntry fileEntry) {
        String path = getFilePath(fileEntry);
        try {
            writeFile(path, getContentFromTmpl(fileEntry));
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
