/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.security;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.jsp.taglib.*;
import org.mmbase.security.Rank; // sucks!

import javax.servlet.jsp.JspTagException;


/**
 * A very simple tag to check for the rank of the current user.
 *
 * @author Michiel Meeuwissen
 * @version $Id: HasRankTag.java,v 1.1 2004-07-15 16:56:03 michiel Exp $
 * @since MMBase-1.8
 */

public class HasRankTag extends CloudReferrerTag implements Condition {

    protected Attribute value    = Attribute.NULL;
    protected Attribute minValue = Attribute.NULL;
    protected Attribute maxValue = Attribute.NULL;
    protected Attribute inverse  = Attribute.NULL;

    public void setValue(String s) throws JspTagException {
        value = getAttribute(s);
    }
    public void setMinvalue(String s) throws JspTagException {
        minValue = getAttribute(s);
    }
    public void setMaxvalue(String s) throws JspTagException {
        maxValue = getAttribute(s);
    }

    public void setInverse(String b) throws JspTagException {
        inverse = getAttribute(b);
    }
    protected boolean getInverse() throws JspTagException {
        return inverse.getBoolean(this, false);
    }

    public int doStartTag() throws JspTagException {
        boolean result;
        String minValueString = minValue.getString(this);
        String maxValueString = maxValue.getString(this);
        String valueString    = value.getString(this);
        if (valueString.length() > 0) {
            if (minValueString.length() > 0 || maxValueString.length() > 0) {
                throw new TaglibException("Cannot specify 'value' attribute with one 'minvalue' or 'maxvalue' attributes.");
            }
            result = getProviderCloudVar().getUser().getRank().equals(valueString);
        } else {
            int rank = Rank.getRank(getProviderCloudVar().getUser().getRank()).getInt();
            result = true;
            if (minValueString.length() > 0) {
                Rank minRank = Rank.getRank(minValueString);
                if (minRank == null) throw new TaglibException("Value of minrank: '" + minValueString + "' is not currently a known rank");
                if (rank < minRank.getInt()) {
                    result = false;
                }
            }
            if (maxValueString.length() > 0) {
                Rank maxRank = Rank.getRank(maxValueString);
                if (maxRank == null) throw new TaglibException("Value of maxrank'" + maxValueString + "' is not currently a known rank");
                if (rank > maxRank.getInt()) {
                    result = false;
                }
            }           
        }


        if (result != getInverse()) {
            return EVAL_BODY;
        } else {
            return SKIP_BODY;
        }
    }
    public int doAfterBody() throws JspTagException {
        if (EVAL_BODY == EVAL_BODY_BUFFERED) { // not needed if EVAL_BODY_INCLUDE
            try{
                if(bodyContent != null) {
                    bodyContent.writeOut(bodyContent.getEnclosingWriter());
                }
            } catch(java.io.IOException e){
                throw new JspTagException("IO Error: " + e.getMessage());
            }
        }
        return SKIP_BODY;
    }

}
