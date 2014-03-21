// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.filesaver;

import webit.generator.core.util.TemplateContextUtil;

/**
 *
 * @author Zqq
 */
public interface FileSaver {

    void init(String outroot);

    boolean copyFile(String file, String src);

    boolean createFolder(String folder);

    boolean saveFile(String tmpl, TemplateContextUtil.FileEntry fileEntry);
}
