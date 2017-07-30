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
package org.febit.generator.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import jodd.io.FileNameUtil;
import jodd.io.StreamUtil;
import org.febit.util.Props;

/**
 *
 * @author zqq90
 */
public class FileUtil {

    private static final String MSG_NOT_A_DIRECTORY = "Not a directory: ";
    private static final String MSG_CANT_CREATE = "Can't create: ";
    private static final String MSG_NOT_FOUND = "Not found: ";
    private static final String MSG_NOT_A_FILE = "Not a file: ";
    private static final String MSG_UNABLE_TO_DELETE = "Unable to delete: ";
    public static final char SYSTEM_SEPARATOR = File.separatorChar;

    public static String getPath(String filename) {
        return FileNameUtil.getFullPathNoEndSeparator(filename);
    }

    public static String getName(String filename) {
        return FileNameUtil.getName(filename);
    }

    public static String normalize(String name) {
        return FileNameUtil.normalize(name);
    }

    public static String normalizeToUnixStyle(String name) {
        return FileNameUtil.normalize(name, true);
    }

    public static String concatWithUnixStyle(String parent, String name) {
        return FileNameUtil.concat(parent, name, true);
    }

    public static String concat(String parent, String name) {
        return FileNameUtil.concat(parent, name);
    }

    public static void writeFile(String basepath, String subpath, byte[] code) throws IOException {
        writeFile(concat(basepath, subpath), code);
    }

    public static void writeFile(String dest, byte[] data) throws IOException {
        FileUtil.mkdirs4File(dest);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(dest, false);
            out.write(data, 0, data.length);
        } finally {
            StreamUtil.close(out);
        }
    }

    public static void deleteFile(String dest) throws IOException {
        deleteFile(new File(dest));
    }

    public static void deleteFile(File dest) throws IOException {
        if (!dest.exists()) {
            throw new FileNotFoundException(MSG_NOT_FOUND + dest);
        }
        if (!dest.isFile()) {
            throw new IOException(MSG_NOT_A_FILE + dest);
        }
        if (!dest.delete()) {
            throw new IOException(MSG_UNABLE_TO_DELETE + dest);
        }
    }

    public static void appendBytes(String dest, byte[] data) throws IOException {
        FileUtil.mkdirs4File(dest);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(dest, true);
            out.write(data, 0, data.length);
        } finally {
            StreamUtil.close(out);
        }
    }

    public static void mkdirs4File(String dest) throws IOException {
        if (dest == null) {
            return;
        }
        mkdirs(getPath(dest));
    }

    public static void mkdirs(String dirs) throws IOException {
        mkdirs(new File(dirs));
    }

    /**
     * Creates all folders at once.
     */
    public static void mkdirs(final File dirs) throws IOException {
        if (dirs.exists()) {
            if (!dirs.isDirectory()) {
                throw new IOException(MSG_NOT_A_DIRECTORY + dirs);
            }
            return;
        }
        if (!dirs.mkdirs()) {
            throw new IOException(MSG_CANT_CREATE + dirs);
        }
    }

    public static Map<String, String> loadResource(String filename) {
        return Props.shadowLoader().load(filename).get().export();
    }

    public static void backupResourceIfExists(final File file) {
        if (file.exists()) {
            file.renameTo(new File(file.getPath() + '.' + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".bak"));
        }
    }
}
