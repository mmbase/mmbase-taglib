/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.jsp.taglib;

import javax.servlet.jsp.*;
import org.springframework.mock.web.*;
import java.util.*;

import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

/**
 * @version $Id: BasicBacking.java 36504 2009-06-30 12:39:45Z michiel $
 */

public  class StringListTagTest {


    @Test
    public void basic() throws Exception {
        final PageContext pageContext = new MockPageContext();


        ContextTag context = new ContextTag();
        context.setPageContext(pageContext);
        context.doStartTag();
        {
            WriteTag i = new WriteTag();
            i.setPageContext(pageContext);
            i.setParent(context);
            i.setId("list");
            i.setVartype("list");
            i.setWrite("false"); // getOut not supported in MockPageContext
            i.setValue("A,B,C");
            i.doStartTag();
            i.doEndTag();
        }
        {
            WriteTag i = new WriteTag();
            i.setPageContext(pageContext);
            i.setId("foo");
            i.setWrite("false"); // getOut not supported in MockPageContext
            i.setValue("bar");
            i.doStartTag();
            i.doEndTag();
        }



        List<String> list = (List<String>) pageContext.getAttribute("list");
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));



        StringListTag tag = new StringListTag();
        tag.setReferid("list");
        tag.setPageContext(pageContext);
        tag.setParent(context);
        int it = tag.doStartTag();
        tag.setBodyContent(null);

        int index = 0;

        while (it == 2) {
            tag.doInitBody();

            {
                WriteTag i = new WriteTag();
                i.setPageContext(pageContext);
                i.setParent(tag);
                assertEquals(tag, i.getContextProvider());
                i.setId("foo");
                i.setWrite("false");
                i.setValue("bla" + (index++));
                i.setReset(true);
                i.doStartTag();
                i.doEndTag();
            }

            it = tag.doAfterBody();
        }
        tag.doEndTag();

        assertEquals("bla2", pageContext.getAttribute("foo")); // MMB-1702
        assertEquals("bla2", context.getObject("foo"));

        context.doEndTag();

    }


}
