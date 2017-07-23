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

import java.io.IOException;
import org.febit.generator.util.FileUtil;
import org.febit.generator.util.Logger;

/**
 *
 * @author zqq90
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
