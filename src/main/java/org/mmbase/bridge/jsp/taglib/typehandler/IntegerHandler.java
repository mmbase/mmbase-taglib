/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.jsp.taglib.typehandler;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.*;
import org.mmbase.storage.search.*;

import org.mmbase.bridge.jsp.taglib.FieldInfoTag;
import org.mmbase.util.logging.Logging;
import org.mmbase.util.logging.Logger;

/**
 * @javadoc
 *
 * @author Gerard van de Looi
 * @author Michiel Meeuwissen
 * @since  MMBase-1.6
 * @version $Id$
 */

public class IntegerHandler extends AbstractTypeHandler {

    private static final Logger log = Logging.getLoggerInstance(IntegerHandler.class);

    /**
     * Constructor for IntegerHandler.
     * @param tag
     */
    public IntegerHandler(FieldInfoTag tag) {
        super(tag);
    }

    /**
     * @see TypeHandler#htmlInput(Node, Field, boolean)
     */
    @Override
    public String htmlInput(Node node, Field field, boolean search) throws JspTagException {
        EnumHandler enumh = getEnumHandler(node, field);
        if (enumh != null) {
            return enumh.htmlInput(node, field, search);
        }
        return super.htmlInput(node, field, search);
    }

    /**
     * @see TypeHandler#useHtmlInput(Node, Field)
     */
    @Override
    public boolean useHtmlInput(Node node, Field field) throws JspTagException {
        log.debug("using html-input");
        EnumHandler enumh = getEnumHandler(node, field);
        if (enumh != null) {
            return enumh.useHtmlInput(node, field);
        }
        return super.useHtmlInput(node, field);
    }

    /**
     * @see TypeHandler#whereHtmlInput(Field)
     */
    @Override
    public String whereHtmlInput(Field field) throws JspTagException {
        EnumHandler enumh = getEnumHandler(null, field);
        if (enumh != null) {
            return enumh.whereHtmlInput(field);
        }
        return super.whereHtmlInput(field);
    }

    @Override
    public Constraint whereHtmlInput(Field field, Query query) throws JspTagException {
        EnumHandler enumh = getEnumHandler(null, field);
        if (enumh != null) {
            return enumh.whereHtmlInput(field, query);
        }
        return super.whereHtmlInput(field, query);
    }

    @Override
    protected Object cast(Object value, Node node, Field field) {
        if (value == null || "".equals(value)) return null;
        return  super.cast(value, node, field);
    }

}
