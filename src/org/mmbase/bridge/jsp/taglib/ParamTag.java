/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.util.Entry;
import javax.servlet.jsp.*;
import java.util.*;
import org.mmbase.util.StringSplitter;
import org.mmbase.util.logging.*;

/**
 * Adds an extra parameter to the parent URL tag.
 *
 * @author Michiel Meeuwissen
 * @version $Id: ParamTag.java,v 1.17 2007-07-14 09:26:49 michiel Exp $
 */

public class ParamTag extends AbstractParamTag implements ParamHandler {
    private static final Logger log = Logging.getLoggerInstance(ParamTag.class);

    private ParamHandler paramHandler;

    public int doStartTag() throws JspException {
        paramHandler = findParentTag(ParamHandler.class, null);
        return super.doStartTag();
    }

    /**
     * @since MMBase-1.9
     */
    protected void addParameter(Object value) throws JspTagException {
        if (name == Attribute.NULL) {
            if (value instanceof CharSequence) {
                for (Map.Entry<String, String> entry : StringSplitter.map(((CharSequence) value).toString()).entrySet()) {
                    paramHandler.addParameter(entry.getKey(), entry.getValue());
                }
            } else {
                throw new TaglibException("You must specifiy a 'name' attribute if the value is not a comma separated String of <name>=<value> pairs.");
            }
        } else {
            paramHandler.addParameter(name.getString(this), value);
        }
    }

    public int doEndTag() throws JspTagException {
        int r = super.doEndTag();
        paramHandler = null;
        return r;

    }

    public void doFinally() {
        paramHandler = null;
        super.doFinally();
    }
}
