/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.functions;

import javax.servlet.jsp.*;

import org.mmbase.bridge.jsp.taglib.Writer;
import org.mmbase.bridge.jsp.taglib.containers.FunctionContainerReferrer;

/**
 * A 'Writer' function tag. The result of the function is available as String and can be written to
 * the page (or the body of the tag can be used to compare it and so
 *
 * @author  Michiel Meeuwissen
 * @since   MMBase-1.7
 * @version $Id$
 */
public class FunctionTag extends AbstractFunctionTag implements Writer, FunctionContainerReferrer {

    @Override
    public int doStartTag() throws JspTagException {
        initTag();
        Object value = getFunctionValue();
        helper.setValue(value);
        return EVAL_BODY_BUFFERED;
    }
    @Override
    public int doAfterBody() throws JspException {
        return helper.doAfterBody();
    }
    @Override
    public int doEndTag() throws JspTagException {
        helper.doEndTag();
        return super.doEndTag();
    }

}
