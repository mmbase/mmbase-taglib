/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.*;
import org.mmbase.bridge.jsp.taglib.containers.ListNodesContainerTag;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.util.Queries;
import org.mmbase.util.logging.*;

/**
 * ListNodesTag, provides functionality for listing single nodes in MMBase
 *
 * @author Kees Jongenburger
 * @author Michiel Meeuwissen
 * @author Pierre van Rooden
 * @version $Id: ListNodesTag.java,v 1.15 2003-10-13 08:34:22 keesj Exp $ 
 */

public class ListNodesTag extends AbstractNodeListTag {
    private static final Logger log = Logging.getLoggerInstance(ListNodesTag.class);

    protected Attribute type      = Attribute.NULL;
    protected Attribute container = Attribute.NULL; 


    public void setContainer(String c) throws JspTagException {
        container = getAttribute(c);
    }

    /**
     * @param type a nodeManager
     */
    public void setType(String t) throws JspTagException {
        type = getAttribute(t);
    }


    protected NodeManager nodeManager;

    /**
     * @since MMBase-1.7
     */
    protected NodeManager getNodeManager() {
        return nodeManager;
    }

    /**
     * @since MMBase-1.7
     */
    protected NodeList getNodes() throws JspTagException {
        ListNodesContainerTag c = (ListNodesContainerTag) findParentTag(ListNodesContainerTag.class, (String) container.getValue(this), false);


        if (c == null || type != Attribute.NULL) {
            if (type == Attribute.NULL) {
                throw new JspTagException("Attribute 'type' must be provided in listnodes tag (unless referid is given, or used in listnodescontainer)");
            }            
            nodeManager = getCloud().getNodeManager(type.getString(this));
            NodeList nodes = nodeManager.getList(constraints.getString(this), (String) orderby.getValue(this), directions.getString(this));
            return nodes;
        } else {
            NodeQuery query = (NodeQuery) c.getQuery();
            // following code will also be necessary in list-tag, so need perhaps be available in AbstractNodeListTag

            Queries.addConstraints(query, (String) constraints.getValue(this));
            Queries.addSortOrders(query, (String) orderby.getValue(this), (String) directions.getValue(this));

            NodeList nodes = getCloud().getList(query);
            return nodes;
        }        
    }

    /**
     * Performs the search
     */
    public int doStartTag() throws JspTagException{
        int superresult =  doStartTagHelper(); // the super-tag handles the use of referid...
        if (superresult != NOT_HANDLED) {
            return superresult;
        }
        NodeList list = getNodes();
        return setReturnValues(list, true);

    }

}

