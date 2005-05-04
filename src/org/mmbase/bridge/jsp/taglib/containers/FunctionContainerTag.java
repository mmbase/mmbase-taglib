/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.containers;

import java.util.*;

import javax.servlet.jsp.JspTagException;

import org.mmbase.util.Entry;
import org.mmbase.bridge.jsp.taglib.functions.AbstractFunctionTag;

/**
 * Function Container can be used around Function (-like) Tags.
 *
 * @author Michiel Meeuwissen
 * @since  MMBase-1.7
 * @version $Id: FunctionContainerTag.java,v 1.13 2005-05-04 22:24:51 michiel Exp $
 */
public class FunctionContainerTag extends AbstractFunctionTag implements FunctionContainer {
    //private static final Logger log = Logging.getLoggerInstance(FunctionContainerTag.class);

    private   List parameters;

    // javadoc inherited (from ParamHandler)
    public void addParameter(String key, Object value) throws JspTagException {
        parameters.add(new Entry(key, value));
    }

    // javadoc inherited (from FunctionContainer)
    public List  getParameters() {
        return Collections.unmodifiableList(parameters);
    }


    public String getName() throws JspTagException {
        return name.getString(this);
    }

    public int doStartTag() throws JspTagException {
        parameters = new ArrayList();
        return EVAL_BODY;
    }

    public int doAfterBody() throws JspTagException {
        if (EVAL_BODY == EVAL_BODY_BUFFERED) {
            try {
                if (bodyContent != null) {
                    bodyContent.writeOut(bodyContent.getEnclosingWriter());
                }
            } catch (java.io.IOException ioe){
                throw new JspTagException(ioe.toString());
            }
        }
        return SKIP_BODY;
    }
    public int doEndTag() throws JspTagException {
        parameters = null;
        return super.doEndTag();
    }


}
