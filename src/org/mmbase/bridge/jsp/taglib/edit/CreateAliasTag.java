/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.edit;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.Node;
import org.mmbase.bridge.jsp.taglib.NodeReferrerTag;

/**
 * To call the method createAlias from Node.
 * 
 * @author Michiel Meeuwissen
 * @version $Id: CreateAliasTag.java,v 1.8 2003-06-06 10:03:19 pierre Exp $
 */

public class CreateAliasTag extends NodeReferrerTag {    

    private Attribute alias = Attribute.NULL;

    public void setName(String n) throws JspTagException {
        alias = getAttribute(n);
    }

    protected void doJob(Node n, String a) {
        n.createAlias(a);
    }

    /**
     * Add the alias.
     * 
     * @todo I think doEndTag is not always called if no body!!
     **/
    public int doEndTag() throws JspTagException {        
        // search the node:
        Node node = getNode();
        
        String a = alias.getString(this);
        // alias name is in the body if no attribute name is given
        if (a.equals("") && bodyContent != null) {
            a = bodyContent.getString();
        } 
        if (! "".equals(a)) {
            doJob(node, a);
        }
        return EVAL_PAGE;
    }
}
