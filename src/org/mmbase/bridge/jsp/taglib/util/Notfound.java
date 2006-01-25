/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.jsp.taglib.util;

import java.util.*;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.jsp.taglib.ContextReferrerTag;
import org.mmbase.util.logging.*;

/**
 * A helper class to implement notfound attribute
 *
 * @author Michiel Meeuwissen
 * @version $Id: Notfound.java,v 1.1 2006-01-25 14:58:17 michiel Exp $
 * @since MMBase-1.8
 */
public abstract class  Notfound  {
    private static final Logger log = Logging.getLoggerInstance(Notfound.class);

    public final static int THROW = 0;
    public final static int SKIP  = 1;
    public final static int PROVIDENULL  = 2;


    public static int get(Attribute notfound,  ContextReferrerTag tag) throws JspTagException {
        if (notfound == Attribute.NULL) {
            return  THROW;
        }
        String is = notfound.getString(tag).toLowerCase();
        if ("skip".equals(is)) {
            return SKIP;
        } else if ("skipbody".equals(is)) {
            return SKIP;
        } else if ("throw".equals(is)) {
            return THROW;
        } else if ("exception".equals(is)) {
            return THROW;
        } else if ("throwexception".equals(is)) {
            return THROW;
        } else if ("null".equals(is)) {
            return PROVIDENULL;
        } else if ("providenull".equals(is)) {
            return  PROVIDENULL;
        } else {
            throw new JspTagException("Invalid value for attribute 'notfound' " + is + "(" + notfound + ")");
        }
    }


}
