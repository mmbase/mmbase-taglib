/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.containers;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.jsp.taglib.*;

import org.mmbase.bridge.*;
import org.mmbase.storage.search.*;

import java.util.*;
import javax.servlet.jsp.JspTagException;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * Cognate of daymarkers.
 *
 * @author Michiel Meeuwissen
 * @since  MMBase-1.7
 * @version $Id: NodeListAgeConstraintTag.java,v 1.1 2003-07-31 20:31:40 michiel Exp $
 * @see    org.mmbase.module.builders.DayMarkers
 */
public class NodeListAgeConstraintTag extends CloudReferrerTag implements NodeListContainerReferrer {

    private static Logger log = Logging.getLoggerInstance(NodeListAgeConstraintTag.class);

    protected Attribute container  = Attribute.NULL;

    protected Attribute field      = Attribute.NULL;
    protected Attribute minAge     = Attribute.NULL;
    protected Attribute maxAge     = Attribute.NULL;

    public void setContainer(String c) throws JspTagException {
        container = getAttribute(c);
    }

    public void setField(String f) throws JspTagException { // default to 'number'
        field = getAttribute(f);
    }

    public void setMinage(String a) throws JspTagException {
        minAge = getAttribute(a);
    }

    public void setMaxage(String a) throws JspTagException {
        maxAge = getAttribute(a);
    }


    

    protected Integer getDayMark(int age) throws JspTagException {
        log.debug("finding day mark for " + age + " days ago");
        NodeManager dayMarks = getCloud().getNodeManager("daymarks");
        NodeQuery query = dayMarks.createQuery();
        StepField step = query.createStepField("daycount");
        int currentDay = (int) (System.currentTimeMillis()/(1000*60*60*24));
        Integer day = new Integer(currentDay  - age);
        Constraint constraint = query.createConstraint(step, FieldCompareConstraint.LESS_EQUAL, day);
        query.setConstraint(constraint);
        query.addSortOrder(query.createStepField("daycount"), SortOrder.ORDER_DESCENDING);
        query.setMaxNumber(1);
        
        NodeList result = dayMarks.getList(query);
        if (result.size() == 0) {
            return new Integer(-1);
        } else {
            return new Integer(result.getNode(0).getIntValue("mark"));
        }
                           

    }



    public int doStartTag() throws JspTagException { 

        if (minAge == Attribute.NULL && maxAge == Attribute.NULL) {
            throw new JspTagException("Either 'minage' or 'maxage' (or both) attributes must be present");
        }
        NodeListContainer c = (NodeListContainer) findParentTag(NodeListContainer.class, (String) container.getValue(this));
        Query query = c.getQuery();

        String fieldName;
        if (field == Attribute.NULL) {
            fieldName = "number";
        } else {
            fieldName = field.getString(this);
        }

        StepField stepField = query.createStepField(fieldName);
        
        Constraint constraint = null;
        
        int minAgeInt = minAge.getInt(this, -1);
        int maxAgeInt = maxAge.getInt(this, -1);

        if (maxAgeInt != -1 && minAgeInt > 0) {
            constraint = query.createConstraint(stepField, getDayMark(maxAgeInt), getDayMark(minAgeInt));
        } else if (maxAgeInt != -1) { // only on max
            constraint = query.createConstraint(stepField, FieldCompareConstraint.GREATER_EQUAL, getDayMark(maxAgeInt));
        } else if (minAgeInt > 0) {
            constraint = query.createConstraint(stepField, FieldCompareConstraint.LESS_EQUAL, getDayMark(minAgeInt));
        } else {
            // both unspecified
        }

        if (constraint != null) {
            query.setConstraint(constraint);
        }
                
        return SKIP_BODY;
    }

}
