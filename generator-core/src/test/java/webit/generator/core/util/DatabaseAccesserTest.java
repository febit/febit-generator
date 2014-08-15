// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.core.util;

import static junit.framework.Assert.assertEquals;
import org.junit.Test;
import webit.generator.core.util.dbaccess.DatabaseAccesser;

/**
 *
 * @author zqq
 */
public class DatabaseAccesserTest {

    @Test
    public void getJdbcType() {
        assertEquals("mysql", DatabaseAccesser.getJdbcType("jdbc:mysql://localhost:3306/sample"));
        assertEquals("oracle", DatabaseAccesser.getJdbcType("jdbc:oracle:thin:@localhost:1521:orcl"));
        assertEquals("db2", DatabaseAccesser.getJdbcType("jdbc:db2://localhost:5000/sample"));
        assertEquals("postgresql", DatabaseAccesser.getJdbcType("jdbc:postgresql://localhost/sample"));
        assertEquals("sqlserver", DatabaseAccesser.getJdbcType("jdbc:sqlserver://localhost:1433;DatabaseName=sample"));
        assertEquals("microsoft", DatabaseAccesser.getJdbcType("jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=sample"));
        assertEquals("sybase", DatabaseAccesser.getJdbcType("jdbc:sybase:Tds:localhost:5007/sample"));
    }
}
