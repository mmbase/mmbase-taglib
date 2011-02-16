/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.jsp.taglib.typehandler;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.*;
import org.mmbase.bridge.jsp.taglib.FieldInfoTag;

/**
 * @javadoc
 *
 * @author Michiel Meeuwissen
 * @since  MMBase-1.9.1
 * @version $Id$
 */

public class ListHandler extends AbstractTypeHandler {

    /**
     * @param tag
     */
    public ListHandler(FieldInfoTag tag) {
        super(tag);
    }
    @Override
    protected EnumHandler getEnumHandler(Node node, Field field) throws JspTagException {
        EnumHandler h = super.getEnumHandler(node, field);
        if (h != null) h.setMultiple(true);
        return h;
    }

}
