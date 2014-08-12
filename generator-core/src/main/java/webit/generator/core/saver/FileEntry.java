// Copyright (c) 2013, Webit Team. All Rights Reserved.

package webit.generator.core.saver;

/**
 *
 * @author zqq
 */
public class FileEntry {
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
