/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.functions;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.jsp.taglib.*;
import org.mmbase.bridge.jsp.taglib.containers.FunctionContainerReferrer;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.util.logging.*;


/**
 * A Function tag for a function with a 'boolean' result. It is a Condition tag, which means that
 * its body is or is not executed, in this case depending on the result of the function.
 *
 * @author  Michiel Meeuwissen
 * @since   MMBase-1.7
 * @version $Id: BooleanFunctionTag.java,v 1.1 2004-01-16 20:21:11 michiel Exp $
 */
public class BooleanFunctionTag extends AbstractFunctionTag implements Condition, FunctionContainerReferrer {

    private static final Logger log = Logging.getLoggerInstance(BooleanFunctionTag.class);

    protected Attribute  inverse      = Attribute.NULL;

    public void setInverse(String b) throws JspTagException {
        inverse = getAttribute(b);
    }

    protected boolean getInverse() throws JspTagException {
        return inverse.getBoolean(this, false);
    }

    public int doStartTag() throws JspTagException {

        Object value = getFunctionValue();
        
        if ("true".equals(value))  value = Boolean.TRUE;
        if ("false".equals(value)) value = Boolean.FALSE;

        if (! (value instanceof Boolean)) {
            throw new JspTagException("Function result '" + value + "' is not of type Boolean but " + (value == null ? value : value.getClass().getName()));
        }
        return (((Boolean) value).booleanValue() != getInverse()) ? EVAL_BODY_BUFFERED : SKIP_BODY;
    }


    public int doAfterBody() throws JspTagException {
        try{
            if(bodyContent != null) {
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
            }
        } catch(java.io.IOException e){
            throw new TaglibException(e);
        }
        return EVAL_PAGE;
    }


}
