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
package org.febit.generator;

import java.util.ArrayList;
import java.util.List;
import org.febit.generator.saver.FileEntry;
import org.febit.generator.saver.FolderEntry;

/**
 *
 * @author zqq90
 */
public class TemplateContext {

    public final static List<FileEntry> files = new ArrayList<FileEntry>();
    public final static List<FolderEntry> folders = new ArrayList<FolderEntry>();

    public static void reset() {
        files.clear();
        folders.clear();
    }

    public static void saveToFile(boolean cancel, int type, String fileName, Object context) {
        files.add(new FileEntry(cancel, type, fileName, context));
    }

    public static void createFolder(int type, String fileName) {
        folders.add(new FolderEntry(false, type, fileName));
    }

    public static void createFolder(boolean cancel, int type, String fileName) {
        folders.add(new FolderEntry(cancel, type, fileName));
    }


}
