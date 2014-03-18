// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

import java.io.IOException;
import webit.script.util.props.Props;

/**
 *
 * @author zqq90
 */
public class PropsUtil {

    public static boolean loadFormClasspath(Props props, String fileName) {
        final char[] data = ResourceUtil.readCharsFromClasspath(fileName);
        if (data != null) {
            props.load(data);
            return true;
        }
        return false;
    }

    public static void loadFormFile(Props props, String fileName) throws IOException {
        props.load(FileUtil.readChars(fileName));
    }

    public static Props createProps() {
        Props props = new Props();
        props.setSkipEmptyProps(false);
        return props;
    }
    
    public static Props createFromClasspath(String fileName) {
        final Props props;
        loadFormClasspath(props =createProps(), fileName);
        return props;
    }
}
