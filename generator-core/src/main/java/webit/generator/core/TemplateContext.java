// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core;

import java.util.ArrayList;
import java.util.List;
import webit.generator.core.saver.FileEntry;
import webit.generator.core.saver.FolderEntry;

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
