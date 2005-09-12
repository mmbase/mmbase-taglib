/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.jsp.taglib.typehandler;

import javax.servlet.jsp.JspTagException;
import org.mmbase.bridge.*;
import org.mmbase.datatypes.DataType;
import org.mmbase.storage.search.Constraint;
import org.mmbase.bridge.jsp.taglib.FieldInfoTag;

import java.util.*;

import org.mmbase.util.SortedBundle;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * This handler can be used to create option list by use of a resource.
 *
 * @author Michiel Meeuwissen
 * @since  MMBase-1.6
 * @version $Id: EnumHandler.java,v 1.23 2005-09-12 17:32:45 michiel Exp $
 */

public class EnumHandler extends AbstractTypeHandler implements TypeHandler {

    private static final Logger log = Logging.getLoggerInstance(EnumHandler.class);
    private Collection bundle;
    private boolean available;
    /**
     * @param tag
     * @since MMBase-1.8
     */
    public EnumHandler(FieldInfoTag tag, Node node,  Field field) throws JspTagException {
        super(tag);
        DataType dataType = field.getDataType();
        Locale locale = tag.getLocale();
        bundle = dataType.getEnumerationValues(locale, tag.getCloudVar(), node, field);
        if (bundle == null) {
            // backwards compatibility mode
            String enumType = field.getGUIType();
            try {
                Class.forName(enumType);
            } catch (Exception ee) {
                try {
                    String resource;
                    if (enumType.indexOf('.') == -1 ) {
                        resource = "org.mmbase.bridge.jsp.taglib.typehandler.resources." + enumType;
                    } else {
                        resource = enumType;

                    }
                    Class type;
                    switch(field.getType()) {
                    case Field.TYPE_STRING:  type = String.class; break;
                    case Field.TYPE_INTEGER: type = Integer.class; break;
                    case Field.TYPE_LONG:    type = Long.class; break;

                        // I wonder if enums for the following types could make any sense, but well:
                    case Field.TYPE_FLOAT:   type = Float.class; break;
                    case Field.TYPE_DOUBLE:  type = Double.class; break;
                    case Field.TYPE_BYTE:    type = byte[].class; break;
                    case Field.TYPE_XML:     type = String.class; break; // Document.class ?
                    case Field.TYPE_NODE:    type = Node.class; break;
                        /*
                          case Field.TYPE_DATETIME:  type = Date.class; break;
                          case Field.TYPE_BOOLEAN:   type = Boolean.class; break;
                          case Field.TYPE_LIST:     //  type = Boolean.class; break;
                        */
                    default: type = String.class;
                    }


                    bundle    = SortedBundle.getResource(resource, tag.getLocale(), getClass().getClassLoader(),
                                                         SortedBundle.NO_CONSTANTSPROVIDER, type, SortedBundle.NO_COMPARATOR).entrySet();
                } catch (java.util.MissingResourceException e) {
                    log.warn(e.toString() + " for field " + field.getName() + " of builder " + field.getNodeManager().getName());
                }
            }
        }
        available = bundle != null;
    }


    protected EnumHandler getEnumHandler(Node node, Field field) throws JspTagException {
        return null;
    }
    public boolean isAvailable() {
        return available;
    }


    public String htmlInput(Node node, Field field, boolean search) throws JspTagException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<select name=\"");
        buffer.append(prefix(field.getName()));
        buffer.append("\" ");
        addExtraAttributes(buffer);
        buffer.append(">\n");
        Object value = null;
        if (node != null) {
            value = node.getValue(field.getName());
        } else {
            value = field.getDataType().getDefaultValue();
        }
        if (! field.getDataType().isRequired()) {
            buffer.append("<option value=\"\" ");
            if (value == null) buffer.append("select=\"selected\" ");
            buffer.append(">--</option>");
        }

        for(Iterator i = bundle.iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            buffer.append("<option value=\"");
            Object key = entry.getKey();
            buffer.append(key);
            buffer.append("\"");
            if ((node != null) && (key.equals(value))) {
                buffer.append(" selected=\"selected\"");
            } else if (search) {
                String searchs = (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(field.getName()));
                if (key.equals(searchs)) {
                    buffer.append(" selected=\"selected\"");
                }
            }
            buffer.append(">");
            buffer.append(entry.getValue());
            buffer.append("</option>\n");
        }
        buffer.append("</select>");
        if (search) {
            String name = prefix(field.getName()) + "_search";
            String searchi =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), name);
            buffer.append("<input type=\"checkbox\" name=\"");
            buffer.append(name);
            buffer.append("\" ");
            if (searchi != null) {
                buffer.append(" checked=\"checked\"");
            }
            buffer.append(" />\n");
        }
        return buffer.toString();
    }


    /**
     * @see TypeHandler#whereHtmlInput(Field)
     */
    public String whereHtmlInput(Field field) throws JspTagException {
        String fieldName = field.getName();
        String id = prefix(fieldName + "_search");
        if ( (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), id) == null) {
            return "";
        } else {
            return super.whereHtmlInput(field);
        }
    }


    public Constraint whereHtmlInput(Field field, Query query) throws JspTagException {
        String fieldName = field.getName();
        String id = prefix(fieldName + "_search");
        if ( (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), id) == null) {
            return null;
        } else {
            return super.whereHtmlInput(field, query);
        }
    }



}
