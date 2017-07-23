/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.generator.util;

import org.junit.Test;
import org.febit.generator.util.dbaccess.DatabaseAccesser;
import static org.junit.Assert.*;

/**
 *
 * @author zqq90
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
