/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.containers;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.*;
import org.mmbase.bridge.jsp.taglib.CloudReferrerTag;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.util.Queries;
import org.mmbase.util.logging.*;

/**
 * Container cognate for ListTag.
 *
 * @author Michiel Meeuwissen
 * @since  MMBase-1.7
 * @version $Id: ListContainerTag.java,v 1.11 2004-07-26 20:18:00 nico Exp $
 */
public class ListContainerTag extends CloudReferrerTag implements QueryContainer {


    private static final Logger log = Logging.getLoggerInstance(ListContainerTag.class);

    private Query   query        = null;
    private Attribute path       = Attribute.NULL;
    private Attribute searchDirs = Attribute.NULL;
    private Attribute fields     = Attribute.NULL;
    protected  Attribute   nodes       = Attribute.NULL;


    public void setPath(String t) throws JspTagException {
        path = getAttribute(t);
    }

    public void setSearchdirs(String s) throws JspTagException {
        searchDirs = getAttribute(s);
    }

    public void setFields(String f) throws JspTagException {
        fields = getAttribute(f);
    }

    /**
     * @since MMBase-1.7.1
     */
    public void setNodes(String n) throws JspTagException {
        nodes = getAttribute(n);
    }


    public Query getQuery() {
        if (query.isUsed()) query = (Query) query.clone();
        return query;
    }


    public int doStartTag() throws JspTagException {        
        if (path == Attribute.NULL) {
            throw new JspTagException("Path attribute is mandatory");
        }
        Cloud cloud = getCloudVar();
        query = cloud.createQuery();

        Queries.addPath(query, (String) path.getValue(this), (String) searchDirs.getValue(this));

        Queries.addFields(query, (String) fields.getValue(this));

        Queries.addStartNodes(query, nodes.getString(this));

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
