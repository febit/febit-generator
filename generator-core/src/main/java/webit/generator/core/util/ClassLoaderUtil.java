// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

/**
 *
 * @author Zqq
 */
public class ClassLoaderUtil {

    public static ClassLoader getDefaultClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
