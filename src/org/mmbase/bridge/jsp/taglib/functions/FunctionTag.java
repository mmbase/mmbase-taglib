/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.functions;

import org.mmbase.bridge.jsp.taglib.Writer;
import javax.servlet.jsp.*;

import org.mmbase.bridge.jsp.taglib.containers.FunctionContainerReferrer;
import org.mmbase.util.logging.*;
import org.mmbase.util.functions.Function;

/**
 * A 'Writer' function tag. The result of the function is available as String and can be written to
 * the page (or the body of the tag can be used to compare it and so 
 *
 * @author  Michiel Meeuwissen
 * @since   MMBase-1.7
 * @version $Id: FunctionTag.java,v 1.1 2004-01-16 20:21:11 michiel Exp $
 */
public class FunctionTag extends AbstractFunctionTag implements Writer, FunctionContainerReferrer {

    private static final Logger log = Logging.getLoggerInstance(FunctionTag.class);

    public int doStartTag() throws JspTagException {     
        helper.setValue(getFunctionValue());
        return EVAL_BODY_BUFFERED;
    }
    public int doAfterBody() throws JspException {
        return helper.doAfterBody();
    }       
    public int doEndTag() throws JspTagException {
        return helper.doEndTag();
    }

}