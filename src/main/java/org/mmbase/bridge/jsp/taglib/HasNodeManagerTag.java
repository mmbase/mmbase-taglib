/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import javax.servlet.jsp.JspTagException;


/**
 * Straight-forward wrapper around {@link org.mmbase.bridge.Cloud#hasNodeManager}.
 *
 * @author Michiel Meeuwissen
 * @version $Id$
 * @since MMBase-1.8
 */

public class HasNodeManagerTag extends CloudReferrerTag implements Condition {

    protected Attribute inverse = Attribute.NULL;
    protected Attribute name    = Attribute.NULL;

    @Override
    public void setInverse(String b) throws JspTagException {
        inverse = getAttribute(b);
    }
    public void setName(String n) throws JspTagException {
        name = getAttribute(n);
    }


    protected boolean getInverse() throws JspTagException {
        return inverse.getBoolean(this, false);
    }


    @Override
    public int doStartTag() throws JspTagException {
        if (getCloudVar().hasNodeManager(name.getString(this)) != getInverse()) {
            return EVAL_BODY;
        } else {
            return SKIP_BODY;
        }
    }
    @Override
    public int doAfterBody() throws JspTagException {
        if (EVAL_BODY == EVAL_BODY_BUFFERED) { // not needed if EVAL_BODY_INCLUDE
            try{
                if(bodyContent != null) {
                    bodyContent.writeOut(bodyContent.getEnclosingWriter());
                }
            } catch(java.io.IOException e){
                throw new JspTagException("IO Error: " + e.getMessage());
            }
        }
        return SKIP_BODY;
    }
}
