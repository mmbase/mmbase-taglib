/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.containers;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.*;
import org.mmbase.bridge.util.Queries;
import org.mmbase.bridge.jsp.taglib.NodeReferrerTag;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.storage.search.*;
import org.mmbase.util.logging.*;

/**
 * Container cognate for ListNodesTag.
 *
 * @author Michiel Meeuwissen
 * @since  MMBase-1.7
 * @version $Id: ListNodesContainerTag.java,v 1.13 2004-07-26 20:18:00 nico Exp $
 */
public class ListNodesContainerTag extends NodeReferrerTag implements NodeQueryContainer { 
    // nodereferrer because RelatedNodesContainer extension


    private static final Logger log = Logging.getLoggerInstance(ListNodesContainerTag.class);

    protected NodeQuery   query       = null;
    protected Attribute   path        = Attribute.NULL;
    protected Attribute   searchDirs  = Attribute.NULL;
    protected Attribute   nodeManager = Attribute.NULL;
    protected  Attribute   element     = Attribute.NULL;
    protected  Attribute   nodes       = Attribute.NULL;


    public void setType(String t) throws JspTagException {
        nodeManager = getAttribute(t);
    }

    public void setPath(String t) throws JspTagException {
        path = getAttribute(t);
    }
    public void setSearchdirs(String s) throws JspTagException {
        searchDirs = getAttribute(s);
    }

    public void setElement(String e) throws JspTagException {
        element = getAttribute(e);
    }

    /**
     * @since MMBase-1.7.1
     */
    public void setNodes(String n) throws JspTagException {
        nodes = getAttribute(n);
    }


    public Query getQuery() {
        return getNodeQuery();
    }

    public NodeQuery getNodeQuery() {
        if (query.isUsed()) query = (NodeQuery) query.clone();
        return query;
    }

    public int doStartTag() throws JspTagException {
        if (nodeManager != Attribute.NULL) {
            query = getCloudVar().getNodeManager(nodeManager.getString(this)).createQuery();
            if (path != Attribute.NULL) throw new JspTagException("Should specify either 'type' or 'path' attributes on listnodescontainer");
            if (element != Attribute.NULL) throw new JspTagException("'element' can only be used in combination with 'path' attribute");
        } else {
            if (path == Attribute.NULL) throw new JspTagException("Should specify either 'type' or 'path' attributes on listnodescontainer");

            query = getCloudVar().createNodeQuery();
            Queries.addPath(query, (String) path.getValue(this), (String) searchDirs.getValue(this));
            
            if (element != Attribute.NULL) {
                String alias = element.getString(this);
                Step nodeStep = query.getStep(alias);
                if (nodeStep == null) { 
                    throw new JspTagException("Could not set element to '" + alias + "' (no such step)");
                }
                query.setNodeStep(nodeStep);
            } else {
                // default to first step
                query.setNodeStep((Step) query.getSteps().get(0));
            }
        }
        if (nodes != Attribute.NULL) {
            Queries.addStartNodes(query, nodes.getString(this));
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
        query = null;
        return super.doEndTag();
    }

}
