/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.pageflow;

import javax.servlet.jsp.JspTagException;

/**
 * The inverse of PresentTag.
 *
 * @see PresentTag
 * @author Michiel Meeuwissen
 */
public class NotPresentTag extends PresentTag {
              
    public int doStartTag() throws JspTagException {
        if (! getContextTag().isPresent(getReferid())) {
            return EVAL_BODY_TAG;
        } else {
            return SKIP_BODY;
        }
    }

}
