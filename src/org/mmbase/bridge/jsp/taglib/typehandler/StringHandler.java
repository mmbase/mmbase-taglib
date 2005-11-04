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
import org.mmbase.datatypes.StringDataType;
import org.mmbase.storage.search.*;
import org.mmbase.util.Encode;
import org.mmbase.util.transformers.Sql;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * A TypeHandler for strings, textareas and text-input.
 * Search values are SQL escaped.
 *
 * @author Gerard van de Looi
 * @author Michiel Meeuwissen
 * @since  MMBase-1.6
 * @version $Id: StringHandler.java,v 1.46 2005-11-04 23:28:23 michiel Exp $
 */

public class StringHandler extends AbstractTypeHandler {

    private static final Logger log = Logging.getLoggerInstance(StringHandler.class);

    /**
     * Constructor for StringHandler.
     */
    public StringHandler(FieldInfoTag tag) {
        super(tag);
    }

    /**
     * @see TypeHandler#htmlInput(Node, Field, boolean)
     */
    public String htmlInput(Node node, Field field, boolean search)        throws JspTagException {
        EnumHandler eh = getEnumHandler(node, field);
        if (eh != null) {
            return eh.htmlInput(node, field, search);
        }

        StringBuffer buffer = new StringBuffer();
        if(! search) {
            StringDataType dataType = (StringDataType) field.getDataType();
            String value = "";
            if (node != null) {
                value = node.getStringValue(field.getName()); 
            } else {
                value = org.mmbase.util.Casting.toString(dataType.getDefaultValue());
            }
            if (value.equals("")) {
                String opt = tag.getOptions();
                if (opt != null && opt.indexOf("noempty") > -1) {
                    value = " ";
                }
            }
            value = tag.decode(value, node);
            if (dataType.getPattern().matcher("\n").matches()) {
                if(field.getMaxLength() > 2048)  {
                    // the wrap attribute is not valid in XHTML, but it is really needed for netscape < 6
                    buffer.append("<textarea wrap=\"soft\" rows=\"10\" cols=\"80\" class=\"big\"");
                } else {
                    buffer.append("<textarea wrap=\"soft\" rows=\"5\" cols=\"80\" class=\"small\" ");
                } 
                addExtraAttributes(buffer);
                buffer.append(" name=\"");
                buffer.append(prefix(field.getName()));
                buffer.append("\">");          
                buffer.append(Encode.encode("ESCAPE_XML", value));
                buffer.append("</textarea>");
            } else { // not 'field' perhaps it's 'string'.
                buffer.append("<input type =\"").append(dataType.isPassword() ? "password" : "text").append("\" class=\"small\" size=\"80\" ");
                buffer.append("name=\"");                
                buffer.append(prefix(field.getName()));
                buffer.append("\" ");
                String opt = tag.getOptions();
                if (opt != null && opt.indexOf("noautocomplete") > -1) { 
                    buffer.append("autocomplete=\"off\" ");
                }                    
                addExtraAttributes(buffer);
                buffer.append(" value=\"");
                buffer.append(Encode.encode("ESCAPE_XML_ATTRIBUTE_DOUBLE", value));
                buffer.append("\" />");
            }
            
            return buffer.toString();
        } else { // in case of search
            return super.htmlInput(node, field, search);
        }
    }

    /**
     * @see TypeHandler#useHtmlInput(Node, Field)
     */
    public boolean useHtmlInput(Node node, Field field) throws JspTagException {
        // do the xml decoding thing...
        String fieldName = field.getName();
        String guiType = field.getGUIType();
        String fieldValue =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName));
        if (log.isDebugEnabled()) {
            log.debug("Received '" + fieldValue + "' for " + field);
        }

        if (guiType.indexOf('.') > 0) {
            EnumHandler eh = new EnumHandler(tag, node, field);
            if (eh.isAvailable()) {
                return eh.useHtmlInput(node, field);
            }
        }

        fieldValue = tag.encode(fieldValue, field);
        if (fieldValue != null && ! fieldValue.equals(node.getValue(fieldName))) {
            if (fieldValue.equals("") && node.getValue(fieldName) == null) return false;
            node.setStringValue(fieldName,  fieldValue);
            return true;
        }

        return false;
    }

    /**
     * @see TypeHandler#whereHtmlInput(Field)
     */
    public String whereHtmlInput(Field field) throws JspTagException {
        EnumHandler eh = getEnumHandler(null, field);
        if (eh != null) {
            return eh.whereHtmlInput(field);
        }
        String search =  findString(field);
        if (search == null) return null;

        Sql sql = new Sql(Sql.ESCAPE_QUOTES);
        return "( UPPER( [" + field.getName() + "] ) LIKE '%" + sql.transform(search.toUpperCase()) + "%')";
        // cannot call getSearvhValue, because sql excaping is done twice then :-(
    }

    protected int getOperator() {
        return FieldCompareConstraint.LIKE;
    }
    protected String getSearchValue(String string) {
        return "%" + string.toUpperCase() + "%";
    }
   public Constraint whereHtmlInput(Field field, Query query) throws JspTagException {
        EnumHandler eh = getEnumHandler(null, field);
        if (eh != null) {
            return eh.whereHtmlInput(field, query);
        }
       Constraint cons =  super.whereHtmlInput(field, query);

       if (cons != null) {
           if (! (cons instanceof FieldConstraint)) {
               throw new JspTagException("Found an instance of " + cons.getClass().getName() + " while expected a field-constraint");
           }
           query.setCaseSensitive((FieldConstraint) cons, false);
       }

       return cons;
   }

}
