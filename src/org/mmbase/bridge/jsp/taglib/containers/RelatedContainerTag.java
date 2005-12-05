/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.containers;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.*;
import org.mmbase.bridge.jsp.taglib.NodeReferrerTag;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.util.Queries;
import org.mmbase.cache.CachePolicy;
import org.mmbase.storage.search.Step;
//import org.mmbase.util.logging.*;

/**
 *
 * @author Michiel Meeuwissen
 * @since  MMBase-1.7
 * @version $Id: RelatedContainerTag.java,v 1.15 2005-12-05 17:21:17 michiel Exp $
 */
public class RelatedContainerTag extends NodeReferrerTag implements QueryContainer {

    //  private static final Logger log = Logging.getLoggerInstance(RelatedContainerTag.class);

    private Query     query      = null;
    private Attribute cachePolicy  = Attribute.NULL;
    private Attribute path       = Attribute.NULL;
    private Attribute searchDirs = Attribute.NULL;
    private Attribute fields     = Attribute.NULL;

    public void setCachepolicy(String t) throws JspTagException {
        cachePolicy = getAttribute(t);
    }

    public void setPath(String t) throws JspTagException {
        path = getAttribute(t);
    }

    public void setSearchdirs(String s) throws JspTagException {
        searchDirs = getAttribute(s);
    }


    public void setFields(String f) throws JspTagException {
        fields = getAttribute(f);
    }

    public Query getQuery() {
        if (query.isUsed()) query = (Query) query.clone();
        return query;
    }



    public int doStartTag() throws JspTagException {
        if (path == Attribute.NULL) {
            throw new JspTagException("Path attribute is mandatory");
        }
        Node node = getNode();
        Cloud cloud = node.getCloud();
        query = cloud.createQuery();

        if (cachePolicy != Attribute.NULL) {
            query.setCachePolicy(CachePolicy.getPolicy(cachePolicy.getValue(this)));
        }


        Step step = query.addStep(node.getNodeManager());
        query.setAlias(step, node.getNodeManager().getName() + "0");
        query.addNode(step, node);

        Queries.addPath(query, (String) path.getValue(this), (String) searchDirs.getValue(this));

        Queries.addFields(query, (String) fields.getValue(this));

        return EVAL_BODY;
    }

    public int doAfterBody() throws JspTagException {
        if(EVAL_BODY == EVAL_BODY_BUFFERED) {
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

    public javax.servlet.jsp.jstl.core.LoopTagStatus getLoopStatus() {
        return new QueryContainerLoopTagStatus(this);
    }
    public Object getCurrent() {
        return null;
    }

}
