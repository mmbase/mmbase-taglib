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
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;
import org.mmbase.util.Arguments;
import org.mmbase.module.core.MMObjectBuilder;
import javax.servlet.jsp.PageContext;

/**
 * @javadoc
 *
 * @author Gerard van de Looi
 * @author Michiel Meeuwissen
 * @since  MMBase-1.6
 * @version $Id: ByteHandler.java,v 1.5 2003-06-11 09:09:10 kees Exp $
 */

public class ByteHandler extends AbstractTypeHandler {
    private static Logger log = Logging.getLoggerInstance(ByteHandler.class);
    /**
     * Constructor for ByteHandler.
     * @param context
     */
    public ByteHandler(FieldInfoTag context) {
        super(context);
    }
    
    /**
     * @see TypeHandler#htmlInput(Node, Field, boolean)
     */
    public String htmlInput(Node node, Field field, boolean search) throws JspTagException {
        Arguments args = new Arguments(MMObjectBuilder.GUI_ARGUMENTS);
        args.set("field", ""); // lot of function implementations would not stand 'null' as field name value
        args.set("language", context.getCloud().getLocale().getLanguage());
        args.set("session",  context.getSessionName());
        PageContext pc = context.getContextTag().getPageContext();
        args.set("response", pc.getResponse());
        args.set("request",  pc.getRequest());
        return  (node != null ? node.getFunctionValue("gui", args).toString() : "") +
                 "<input type=\"" + (search ? "text" : "file") + "\" name=\"" + prefix(field.getName()) + "\" />";
    }
    
    /**
     * @see TypeHandler#useHtmlInput(Node, Field)
     */
    public String useHtmlInput(Node node, Field field) throws JspTagException {        
        String fieldName = field.getName();
        byte [] bytes  = context.getContextTag().getBytes(prefix(fieldName));
        if (bytes.length > 0) {
            node.setByteValue(fieldName, bytes);
        }
        return "";
    }
    
    /**
     * @see TypeHandler#whereHtmlInput(Field)
     */
    public String whereHtmlInput(Field field) throws JspTagException {
        log.error("Don't know what to do with byte[]");
        return super.whereHtmlInput(field);
    }
    
}
