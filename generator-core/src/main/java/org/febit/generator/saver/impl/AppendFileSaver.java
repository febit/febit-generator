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
package org.febit.generator.saver.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.febit.generator.saver.AbstractAppendFileSaver;
import org.febit.generator.saver.FileEntry;
import org.febit.generator.util.FileUtil;
import org.febit.generator.util.Logger;

/**
 *
 * @author zqq90
 */
public class AppendFileSaver extends AbstractAppendFileSaver {

    protected String outroot;
    protected final Set<String> clearedFiles = new HashSet<>();

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
