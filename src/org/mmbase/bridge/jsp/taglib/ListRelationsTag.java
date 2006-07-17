/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.jsp.taglib.containers.ListRelationsContainerTag;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.*;
import org.mmbase.bridge.util.Queries;

/**
 * ListRelationsTag, a tag around bridge.Node.getRelations.
 *
 * @author Michiel Meeuwissen
 * @version $Id: ListRelationsTag.java,v 1.20 2006-07-17 15:38:47 johannes Exp $
 */

public class ListRelationsTag extends AbstractNodeListTag {

    private Attribute type = Attribute.NULL;
    private Attribute role = Attribute.NULL;
    private Attribute searchDir = Attribute.NULL;
    protected Attribute container = Attribute.NULL;

    private NodeManager nm;
    private NodeList relatedNodes = null;
    private NodeQuery relatedQuery = null;
    private Node     relatedFromNode;


    Node getRelatedfromNode() {
        NodeList returnList = getReturnList();
        return returnList == null ? null : (Node) returnList.getProperty("relatedFromNode");
    }


    public void setContainer(String c) throws JspTagException {
        container = getAttribute(c);
    }


    /**
     * @param t The name of a node manager
     */
    public void setType(String t) throws JspTagException {
        type  = getAttribute(t);
    }
    /**
     * @param r a role
     */
    public void setRole(String r) throws JspTagException {
        role  = getAttribute(r);
    }

    public void setSearchdir(String s) throws JspTagException {
        searchDir = getAttribute(s);
    }


    protected NodeQuery getRelatedQuery() throws JspTagException {
        if (relatedQuery == null) {
            relatedQuery = Queries.createRelatedNodesQuery(relatedFromNode, nm, (String) role.getValue(this), (String) searchDir.getValue(this));
            if (orderby != Attribute.NULL) {
                Queries.addSortOrders(relatedQuery, (String) orderby.getValue(this), (String) directions.getValue(this));
            }
            Queries.sortUniquely(relatedQuery);
        }
        return relatedQuery;
    }

    public Node getRelatedNode() throws JspTagException {
        if (relatedNodes == null) {
            NodesAndTrim result = getNodesAndTrim(getRelatedQuery());
            relatedNodes = result.nodeList;
            if (getId() != null) {
                getRelatedQuery();
                listHelper.getReturnList().setProperty("relatedNodes", relatedNodes);
            }

        }
        int i = listHelper.getIndex();
        if (i >= relatedNodes.size()) i = relatedNodes.size() - 1;
        if (i < 0) i = 0;
        return relatedNodes.getNode(i);

    }

    public int doStartTag() throws JspTagException{
        int superresult =  doStartTagHelper(); // the super-tag handles the use of referid...
        if (superresult != NOT_HANDLED) {
            relatedFromNode = (Node)      listHelper.getReturnList().getProperty("relatedFromNode");
            relatedQuery    = (NodeQuery) listHelper.getReturnList().getProperty("relatedQuery");
            relatedNodes    = (NodeList)  listHelper.getReturnList().getProperty("relatedNodes");
            return superresult;
        }

        ListRelationsContainerTag c = (ListRelationsContainerTag) findParentTag(ListRelationsContainerTag.class, (String) container.getValue(this), false);

        relatedNodes = null;
        relatedQuery = null;

        NodeQuery query;
        if (c == null || type != Attribute.NULL || role != Attribute.NULL || searchDir != Attribute.NULL) { // containerless version
            // obtain a reference to the node through a parent tag
            relatedFromNode = getNode();
            if (relatedFromNode == null) {
                throw new JspTagException("Could not find parent node!!");
            }

            nm = null;
            if (type != Attribute.NULL) {
                nm = relatedFromNode.getCloud().getNodeManager(type.getString(this));
            }

            query = Queries.createRelationNodesQuery(relatedFromNode, nm, (String) role.getValue(this), (String) searchDir.getValue(this));
            relatedQuery = null; // determin when needed
        } else { // working with container
            query = (NodeQuery) c.getQuery();
            relatedQuery = c.getRelatedQuery();
            relatedQuery.setOffset(query.getOffset());
            relatedQuery.setMaxNumber(query.getMaxNumber());
            relatedFromNode = c.getRelatedFromNode();
            if (orderby != Attribute.NULL) {
                Queries.addSortOrders(relatedQuery, (String) orderby.getValue(this), (String) directions.getValue(this));
            }
            Queries.sortUniquely(relatedQuery);
        }


        if (orderby != Attribute.NULL) {
            Queries.addSortOrders(query,        (String) orderby.getValue(this), (String) directions.getValue(this));
        }

        Queries.sortUniquely(query);

        NodesAndTrim result = getNodesAndTrim(query);
        result.nodeList.setProperty("relatedFromNode", relatedFromNode); // used to be used by mm:relatednode but not any more.


        if (getId() != null) {
            getRelatedQuery();
            result.nodeList.setProperty("relatedQuery", relatedQuery);
        }

        return setReturnValues(result.nodeList, result.needsTrim);
    }

    public int doEndTag() throws JspTagException {
        return super.doEndTag();
    }

    public void doFinally() {
        nm = null;
        relatedNodes = null;
        relatedQuery = null;
        relatedFromNode = null;
        super.doFinally();
    }
}

