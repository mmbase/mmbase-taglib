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
import org.mmbase.storage.search.*;
import org.mmbase.util.Encode;
import org.mmbase.util.logging.*;
import org.mmbase.util.transformers.Sql;

/**
 * A TypeHandler for strings, textareas and text-input.
 * Search values are SQL escaped.
 *
 * @author Gerard van de Looi
 * @author Michiel Meeuwissen
 * @since  MMBase-1.6
 * @version $Id: StringHandler.java,v 1.19 2003-09-26 18:44:29 michiel Exp $
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

        StringBuffer buffer = new StringBuffer();
        if(! search) {
            if (field.getName().equals("owner")) {
                Cloud cloud = tag.getCloud();
                if (node == null) {
                    buffer.append(cloud.getUser().getOwnerField());
                } else if (! node.mayChangeContext()) {
                    buffer.append(node.getContext());
                } else {

                    String value = node.getContext();
                    buffer.append("<select name=\"" + prefix("owner") + "\">\n");


                    StringList possibleContexts = node.getPossibleContexts();

                    if (! possibleContexts.contains(value)) {
                        possibleContexts.add(0, value);
                    }
                    StringIterator i = possibleContexts.stringIterator();
                    while (i.hasNext()) {
                        String listContext = i.nextString();
                        buffer.append("  <option ");
                        if (value.equals(listContext)){
                            buffer.append("selected=\"selected\"");
                        }
                        buffer.append("value=\"" + listContext+ "\">");
                        buffer.append(listContext);
                        buffer.append("</option>\n");
                    }
                    buffer.append("</select>");
                }
            } else if(field.getMaxLength() > 2048)  {
                // the wrap attribute is not valid in XHTML, but it is really needed for netscape < 6
                buffer.append("<textarea wrap=\"soft\" rows=\"10\" cols=\"80\" class=\"big\"  name=\"");
                buffer.append(prefix(field.getName()));
                buffer.append("\">");
                if (node != null) {
                    buffer.append(Encode.encode("ESCAPE_XML", tag.decode(node.getStringValue(field.getName()), node)));
                }
                buffer.append("</textarea>");
            } else if(field.getMaxLength() > 255 )  {
                buffer.append("<textarea wrap=\"soft\" rows=\"5\" cols=\"80\" class=\"small\"  name=\"");
                buffer.append(prefix(field.getName()));
                buffer.append("\">");
                if (node != null) {
                    buffer.append(Encode.encode("ESCAPE_XML", tag.decode(node.getStringValue(field.getName()), node)));
                }
                buffer.append("</textarea>");
            } else {
                if (field.getGUIType().equals("password")) {
                    buffer.append("<input type =\"password\" class=\"small\" size=\"80\" name=\"");
                } else {
                    buffer.append("<input type =\"text\" class=\"small\" size=\"80\" name=\"");
                }
                buffer.append(prefix(field.getName()));
                buffer.append("\" value=\"");
                if (node != null) {
                    buffer.append(Encode.encode("ESCAPE_XML_ATTRIBUTE_DOUBLE", tag.decode(node.getStringValue(field.getName()), node)));
                }
                buffer.append("\" />");
            }
            return buffer.toString();
        } else {
            return super.htmlInput(node, field, search);
        }
    }

    /**
     * @see TypeHandler#useHtmlInput(Node, Field)
     */
    public String useHtmlInput(Node node, Field field) throws JspTagException {
        // do the xml decoding thing...
        String fieldName = field.getName();
        String fieldValue =  (String) tag.getContextProvider().getContextContainer().find(tag.getPageContext(), prefix(fieldName));
        if (fieldName.equals("owner")) {
            if (fieldValue != null && ! fieldValue.equals(node.getContext())) {
                node.setContext(fieldValue);
            }
        } else {
            fieldValue = tag.encode(fieldValue, field);
            if (fieldValue != null && ! fieldValue.equals(node.getValue(fieldName))) {
                node.setValue(fieldName,  fieldValue);
            }
        }
        return "";
    }

    /**
     * @see TypeHandler#whereHtmlInput(Field)
     */
    public String whereHtmlInput(Field field) throws JspTagException {
        String search =  findString(field);
        if (search == null) return null;

        Sql sql = new Sql(Sql.ESCAPE_QUOTES);
        return "( UPPER( [" + field.getName() + "] ) LIKE '%" + sql.transform(search) + "%')";
    }

    protected int getOperator() {
        return FieldCompareConstraint.LIKE;
    }
    protected String getSearchValue(String string) {
        return "%" + string.toUpperCase() + "%";
    }
   public Constraint whereHtmlInput(Field field, Query query) throws JspTagException {
       FieldConstraint cons = (FieldConstraint) super.whereHtmlInput(field, query);
       if (cons != null) {
           query.setCaseSensitive(cons, false);
       }
       return cons;
   }

}
