// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zqq90
 */
public class TemplateContextUtil {

    public static class FileEntry {

        public final boolean cancel;
        public final int type;
        public final String fileName;
        public final Object context;

        public FileEntry(boolean cancel, int type, String fileName, Object context) {
            this.cancel = cancel;
            this.type = type;
            this.fileName = fileName;
            this.context = context;
        }
    }

    public static class FolderEntry extends FileEntry {

        public FolderEntry(boolean cancel, int type, String fileName) {
            super(cancel, type, fileName, null);
        }
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

    public final static List<FileEntry> files = new ArrayList<FileEntry>();
    public final static List<FolderEntry> folders = new ArrayList<FolderEntry>();

    public static void reset() {
        files.clear();
        folders.clear();
    }
}
