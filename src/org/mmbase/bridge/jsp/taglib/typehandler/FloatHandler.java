/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.jsp.taglib.typehandler;

import javax.servlet.jsp.JspTagException;
import org.mmbase.bridge.Field;
import org.mmbase.bridge.Node;
import org.mmbase.bridge.jsp.taglib.FieldInfoTag;

/**
 * @author Gerard van de Looi
 * @author Michiel Meeuwissen
 * @since  MMBase-1.6
 */
public class FloatHandler extends AbstractTypeHandler {

    /**
     * Constructor for FloatHandler.
     * @param context
     */
    public FloatHandler(FieldInfoTag context) {
        super(context);
    }

}