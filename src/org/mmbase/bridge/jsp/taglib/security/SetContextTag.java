/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.security;
import org.mmbase.bridge.jsp.taglib.NodeReferrerTag;
import javax.servlet.jsp.JspTagException;


/**
* A very simple tag to change the context of the node
* 
* @author Michiel Meeuwissen
*/
public class SetContextTag extends NodeReferrerTag {

    public int doAfterBody() throws JspTagException {        
        getNode().setContext(bodyContent.getString());
        return EVAL_PAGE;
    }   

}
