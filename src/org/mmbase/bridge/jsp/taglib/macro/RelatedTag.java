/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.macro;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.Node;

import org.mmbase.bridge.jsp.taglib.ListTag;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.jsp.taglib.containers.*;

//import org.mmbase.util.logging.*;


/**
 * Shortcut for List where the start node is the parent node.
 *
 * @author Michiel Meeuwissen
 * @author Jacco de Groot
 * @author Pierre van Rooden
 * @version $Id: RelatedTag.java,v 1.30 2005-06-20 16:03:38 michiel Exp $
 */
public class RelatedTag extends ListTag {
    // private static final Logger log = Logging.getLoggerInstance(RelatedTag.class);

    protected Node getBaseNode() throws JspTagException {
        if (nodes != Attribute.NULL && ! nodes.getString(this).equals("")) {
            // return getCloudVar().getNode((String)StringSplitter.split(nodes.getString(this), ",").get(0));
            String[] ns = nodes.getString(this).trim().split("\\s*,\\s*");
            return getCloudVar().getNode(ns[0]);
        } else {
            return getNode();
        }
    }

    protected String getSearchNodes() throws JspTagException {
        return (nodes == Attribute.NULL || nodes.getString(this).equals("")) ? "" + getNode().getNumber() : nodes.getString(this);
    }

    /**
     * Obtain the list path. Adds the related basenode's type (extended with a '0' postfix to distinguis it from
     * other types in the path) to the front of the path.
     */
    protected String getPath() throws JspTagException {
        return getBaseNode().getNodeManager().getName() + "0," + path.getString(this);
    }

    protected QueryContainer getListContainer() throws JspTagException {
        return (QueryContainer) findParentTag(RelatedContainerTag.class, (String) container.getValue(this), false);
    }

}
