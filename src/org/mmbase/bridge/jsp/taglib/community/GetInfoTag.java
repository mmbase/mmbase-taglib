/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.community;

import  org.mmbase.bridge.jsp.taglib.util.Attribute;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspException;


import org.mmbase.bridge.Node;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

import org.mmbase.bridge.jsp.taglib.*;

/**
 * GetInfo tag obtains information from the multipurpose INFO field.
 *
 * @author Pierre van Rooden
 * @author Michiel Meeuwissen
 * @version $Id: GetInfoTag.java,v 1.12 2003-06-18 13:41:30 michiel Exp $
 */
 
public class GetInfoTag extends NodeReferrerTag implements Writer {

    private static Logger log = Logging.getLoggerInstance(GetInfoTag.class);

    private Attribute key = Attribute.NULL;
    public void setKey(String k) throws JspTagException {
        key = getAttribute(k);
    }


    public int doStartTag() throws JspTagException{
        // firstly, search the node:
        Node node = getNode();

        String k;
        // found the node now. Now we can decide what must be shown:
        if (key == Attribute.NULL) { 
            k = "name";
        } else {
            k = key.getString(this);
        }
        String value = node.getStringValue("getinfovalue(" + k + ")");
        if (value == null) value = "";
        helper.setTag(this);
        helper.setValue(value);
        helper.useEscaper(false); // fieldinfo typicaly produces xhtml
        if (getId() != null) {
            getContextProvider().getContainer().register(getId(), helper.getValue());
        }
        return EVAL_BODY_BUFFERED;
    }


    /**
     * write the value of the field.
     **/
    public int doEndTag() throws JspTagException {
        return helper.doEndTag();
    }

    public int doAfterBody() throws JspException {
        return helper.doAfterBody();
    }
}
