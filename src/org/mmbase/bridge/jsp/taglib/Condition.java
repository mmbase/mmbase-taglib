/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;
import javax.servlet.jsp.JspTagException;

/**
 * Interface for 'condition' tags. ConditionTags are things like
 * 'listcondition' (and descendants like 'first' and 'last'), present,
 * compare. These tags are similar, therefore all implement this ConditionTag interface.
 *
 * @author Michiel Meeuwissen
 **/
public interface Condition {

    /**
     * This attribute inverses the sense of the condition.
     *
     **/
    public void setInverse(String b) throws JspTagException;
}
