// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.util;

import java.util.List;

/**
 *
 * @author Zqq
 */
public class CollectionUtil {

    public static List adds(List list, Object[] objs) {
        if (objs != null) {
            list.addAll(java.util.Arrays.asList(objs));
        }
        return list;
    }
}
