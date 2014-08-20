// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.util;

import static junit.framework.Assert.*;
import org.junit.Test;

/**
 *
 * @author zqq
 */
public class NamingUtilTest {

    @Test
    public void toLowerCamelCaseTest() {

        assertEquals("userName", NamingUtil.toLowerCamelCase("user_name"));
        assertEquals("userName", NamingUtil.toLowerCamelCase("USER_NAME"));
        assertEquals("userName", NamingUtil.toLowerCamelCase("uSer_name"));
        assertEquals("userName", NamingUtil.toLowerCamelCase("USeR_nAME"));
        assertEquals("userName", NamingUtil.toLowerCamelCase("_user__name"));
        assertEquals("userName", NamingUtil.toLowerCamelCase("__USER__NAME"));
    }
}
