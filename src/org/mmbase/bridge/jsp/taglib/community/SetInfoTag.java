/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.community;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.Node;
import org.mmbase.bridge.Module;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

import org.mmbase.bridge.jsp.taglib.*;

/**
* SetInfo tag stores information in the multipurpose INFO field.
*
* @author Pierre van Rooden
*/
public class SetInfoTag extends NodeReferrerTag {

    private static Logger log = Logging.getLoggerInstance(FieldTag.class.getName());

    protected Node node;
    private String key=null;

    public void setKey(String k) throws JspTagException {
        key = getAttributeValue(k);
    }

    public int doStartTag() throws JspTagException{
        // firstly, search the node:
        node = getNode();

        if (key == null) { // name not null
            throw new JspTagException ("Should use 'key' attribute");
        }
        return EVAL_BODY_TAG;
    }

    /**
     * store the given value
     **/
    public int doAfterBody() throws JspTagException {
        String value=bodyContent.getString();
        Module community=getCloudContext().getModule("communityprc");
        community.getInfo("MESSAGE-"+node.getNumber()+"-SETINFOFIELD-"+value,pageContext.getRequest(),pageContext.getResponse());
        return SKIP_BODY;
    }
}
