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
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.jsp.taglib.util.Referids;

/**
 * Function Container can be used around Function (-like) Tags.
 *
 * @author Michiel Meeuwissen
 * @since  MMBase-1.7
 * @version $Id: FunctionContainerTag.java,v 1.17 2007-07-14 09:26:49 michiel Exp $
 */
public class FunctionContainerTag extends AbstractFunctionTag implements FunctionContainer {
    //private static final Logger log = Logging.getLoggerInstance(FunctionContainerTag.class);

    private  List<Entry<String, Object>> parameters ;

    // javadoc inherited (from ParamHandler)
    public void addParameter(String key, Object value) {
        parameters.add(new Entry<String, Object>(key, value));
    }
    

    // javadoc inherited (from FunctionContainer)
    public List<Entry<String, Object>>  getParameters() {
        return Collections.unmodifiableList(parameters);
    }


    public String getName() throws JspTagException {
        return name.getString(this);
    }

    public int doStartTag() throws JspTagException {
        parameters = new ArrayList<Entry<String, Object>>();
        if (referids != Attribute.NULL) {
            for (Map.Entry<String, Object> entry : Referids.getReferids(referids, this).entrySet()) {
                addParameter(entry.getKey(), entry.getValue());    
            } 
        }
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
