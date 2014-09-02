// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import webit.script.util.CharArrayWriter;

/**
 *
 * @author ZQQ
 */
public class FileUtil {

    private static final String MSG_NOT_A_DIRECTORY = "Not a directory: ";
    private static final String MSG_CANT_CREATE = "Can't create: ";
    private static final String MSG_NOT_FOUND = "Not found: ";
    private static final String MSG_NOT_A_FILE = "Not a file: ";
    private static final String MSG_UNABLE_TO_DELETE = "Unable to delete: ";

    public static final char UNIX_SEPARATOR = '/';
    public static final char WINDOWS_SEPARATOR = '\\';
    public static final char SYSTEM_SEPARATOR = File.separatorChar;
    public static final char OTHER_SEPARATOR;

    static {
        if (SYSTEM_SEPARATOR == WINDOWS_SEPARATOR) {
            OTHER_SEPARATOR = UNIX_SEPARATOR;
        } else {
            OTHER_SEPARATOR = WINDOWS_SEPARATOR;
        }
    }

    public static String getPath(String filename) {
        filename = normalize(filename);
        int i = filename.lastIndexOf(SYSTEM_SEPARATOR);
        if (i >= 0) {
            return filename.substring(0, i + 1);
        } else {
            return StringUtil.EMPTY;
        }
    }

    public static String getName(String filename) {
        filename = normalize(filename);
        int i = filename.lastIndexOf(SYSTEM_SEPARATOR);
        if (i >= 0) {
            return filename.substring(i + 1);
        } else {
            return filename;
        }
    }

    public static String normalize(String name) {
        return name.replace(OTHER_SEPARATOR, SYSTEM_SEPARATOR);
    }

    public static String normalizeToUnixStyle(String name) {
        return name.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
    }

    public static String concatWithUnixStyle(String parent, String name) {
        parent = normalizeToUnixStyle(parent);
        if (name.length() != 0) {
            name = normalizeToUnixStyle(name);
            char firstChar = name.charAt(0);
            if (parent.charAt(parent.length() - 1) == UNIX_SEPARATOR) {
                if (firstChar == UNIX_SEPARATOR) {
                    return parent.concat(name.substring(1));
                } else {
                    return parent.concat(name);
                }
            } else {
                if (firstChar == UNIX_SEPARATOR) {
                    return parent.concat(name);
                } else {
                    return parent + UNIX_SEPARATOR + name;
                }
            }
        } else {
            return parent;
        }
    }

    public static String concat(String parent, String name) {
        parent = normalize(parent);
        if (name.length() != 0) {
            name = normalize(name);
            char firstChar = name.charAt(0);
            if (parent.charAt(parent.length() - 1) == SYSTEM_SEPARATOR) {
                if (firstChar == SYSTEM_SEPARATOR) {
                    return parent.concat(name.substring(1));
                } else {
                    return parent.concat(name);
                }
            } else {
                if (firstChar == SYSTEM_SEPARATOR) {
                    return parent.concat(name);
                } else {
                    return parent + SYSTEM_SEPARATOR + name;
                }
            }
        } else {
            return parent;
        }
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
        if (dest.exists() == false) {
            throw new FileNotFoundException(MSG_NOT_FOUND + dest);
        }
        if (dest.isFile() == false) {
            throw new IOException(MSG_NOT_A_FILE + dest);
        }
        if (dest.delete() == false) {
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
    public static void mkdirs(File dirs) throws IOException {
        if (dirs.exists()) {
            if (dirs.isDirectory() == false) {
                throw new IOException(MSG_NOT_A_DIRECTORY + dirs);
            }
            return;
        }
        if (dirs.mkdirs() == false) {
            throw new IOException(MSG_CANT_CREATE + dirs);
        }
    }

    public static char[] readChars(String file) throws IOException {
        return readChars(new File(file), "UTF-8");
    }

    public static char[] readChars(File file) throws IOException {
        return readChars(file, "UTF-8");
    }

    public static char[] readChars(File file, String encoding) throws IOException {
        if (file.exists() == false) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if (file.isFile() == false) {
            throw new IOException(file.getAbsolutePath());
        }
        long len = (int) file.length();
        if (len >= Integer.MAX_VALUE) {
            len = Integer.MAX_VALUE;
        }
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            CharArrayWriter fastCharArrayWriter = new CharArrayWriter((int) len);
            StreamUtil.copy(in, fastCharArrayWriter, encoding);
            return fastCharArrayWriter.toArray();
        } finally {
            StreamUtil.close(in);
        }
    }

}
