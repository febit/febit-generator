// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.util;

import org.junit.Test;
import webit.script.Engine;
import webit.script.Template;
import webit.script.exceptions.ResourceNotFoundException;

/**
 *
 * @author zqq
 */
public class WitParserPerformanceTest {

    @Test
    public void test() throws ResourceNotFoundException {
        final Engine engine = Engine.create();
        final String raw = "string: <%\n"
                + "var assertEquals = function(){};"
                + "var assertTrue = function(){};"
                + "\n"
                + "{\n"
                + "\n"
                + "    assertEquals(3, 1+2);\n"
                + "    assertEquals(-1, 1-2);\n"
                + "    assertEquals(4, 2*2);\n"
                + "    assertEquals(2, 5/2);\n"
                + "    assertEquals(1, 5%2);\n"
                + "    assertEquals(20,5 << 2);\n"
                + "    assertEquals(1, 5 >> 2);\n"
                + "    assertEquals(-2, -5 >> 2);\n"
                + "    assertEquals(1073741822, -5 >>> 2);\n"
                + "\n"
                + "    //chars\n"
                + "    //a : 97\n"
                + "    var a = 'a';\n"
                + "    var temp = 'a' +1;\n"
                + "\n"
                + "    assertTrue(97 >= a);\n"
                + "    assertTrue(97 <= a);\n"
                + "    assertTrue(97 == a);\n"
                + "    assertTrue(98 >= a);\n"
                + "    assertTrue(98 > a);\n"
                + "    assertTrue(90 < a);\n"
                + "    assertTrue(90 <= a);\n"
                + "\n"
                + "    assertEquals(97 +1 , temp);\n"
                + "    assertEquals(97 +1 , a +1);\n"
                + "    assertEquals(97 +1 , a +1);\n"
                + "    assertEquals(97 +1 , 1+ a);\n"
                + "    assertEquals(1- 97 , 1- a);\n"
                + "    assertEquals(97 -1 , a -1);\n"
                + "\n"
                + "    assertEquals(97/2 , a/2);\n"
                + "    assertEquals(97 * 2 , a * 2);\n"
                + "    assertEquals(97 >> 2 , a >> 2);\n"
                + "    assertEquals(97 % 2 , a % 2);\n"
                + "\n"
                + "    // ?:\n"
                + "    assertEquals(1, true?1:2);\n"
                + "    assertEquals(1, 1?:2);\n"
                + "    assertEquals(2, null?:2);\n"
                + "\n"
                + "    \n"
                + "    assertEquals(\"11\", 1 +\"1\");\n"
                + "\n"
                + "    assertEquals(\"c1\", \"c\" + '1');\n"
                + "\n"
                + "}\n"
                + "{\n"
                + "    //Int Step test\n"
                + "\n"
                + "    //asc\n"
                + "    var one = 1;\n"
                + "    var tree = 3; \n"
                + "    var i = 1;\n"
                + "    for(num : i .. tree){\n"
                + "        assertEquals(i, num);\n"
                + "        i++;\n"
                + "    }\n"
                + "    assertEquals(i, 4);\n"
                + "\n"
                + "    //desc\n"
                + "    i = 3;\n"
                + "    for(num : i .. one){\n"
                + "        assertEquals(i, num);\n"
                + "        i--;\n"
                + "    }\n"
                + "    assertEquals(i, 0);\n"
                + "\n"
                + "}";
        final Template template = engine.getTemplate(raw);
        template.reload();
        template.reload();
        template.reload();

        long start = System.currentTimeMillis();

        
        
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();
        template.reload();

        System.out.println(System.currentTimeMillis() - start);

    }
}
