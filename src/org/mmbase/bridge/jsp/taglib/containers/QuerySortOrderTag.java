/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.containers;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.Query;
import org.mmbase.bridge.jsp.taglib.CloudReferrerTag;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.storage.search.*;
//import org.mmbase.util.logging.*;

/**
 * Applies a sortorder to the surrounding query.
 *
 * @author Michiel Meeuwissen
 * @since  MMBase-1.7
 * @version $Id: QuerySortOrderTag.java,v 1.1 2003-12-18 09:05:48 michiel Exp $
 */
public class QuerySortOrderTag extends CloudReferrerTag implements QueryContainerReferrer {

    //private static final Logger log = Logging.getLoggerInstance(QuerySortOrderTag.class);

    protected Attribute container  = Attribute.NULL;
    protected Attribute direction  = Attribute.NULL;
    protected Attribute field      = Attribute.NULL;

    public void setContainer(String c) throws JspTagException {
        container = getAttribute(c);
    }

    public void setDirection(String d) throws JspTagException {
        direction = getAttribute(d);
    }
    public void setField(String f) throws JspTagException {
        field = getAttribute(f);
    }


    public int doStartTag() throws JspTagException { 
        QueryContainer c = (QueryContainer) findParentTag(QueryContainer.class, (String) container.getValue(this));
        
        String dir = direction.getString(this).toUpperCase();
        
        Query query = c.getQuery();
        
        int order;
        
        if (dir.equals("")) {
            order = SortOrder.ORDER_ASCENDING;
        } else if (dir.equals("UP")) {
            order = SortOrder.ORDER_ASCENDING;
        } else if (dir.equals("DOWN")) {
            order = SortOrder.ORDER_DESCENDING;
        } else if (dir.equals("ASCENDING")) {
            order = SortOrder.ORDER_ASCENDING;
        } else if (dir.equals("DESCENDING")) {
            order = SortOrder.ORDER_DESCENDING;
        } else {
            throw new JspTagException("Unknown sort-order '" + dir + "'");
        }
        
        StepField stepField = query.createStepField(field.getString(this));
                   
        query.addSortOrder(stepField, order);
        return SKIP_BODY;
    }

}