/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.edit;

import javax.servlet.jsp.*;
import org.mmbase.bridge.*;
import org.mmbase.bridge.jsp.taglib.*;
import org.mmbase.util.logging.*;

/**
 * The SetFieldTag can be used as a child of a 'NodeProvider' tag or inside a
 * FieldProvider.
 *
 * @author Michiel Meeuwissen
 * @author Jaco de Groot
 */
public class SetFieldTag extends FieldTag { // but it is not a writer
    private static Logger log = Logging.getLoggerInstance(SetFieldTag.class.getName());

    protected String convert (String s) throws JspTagException {
        return s;
    }

    public int doStartTag() throws JspTagException {
        node= null;
        setFieldVar(name);
        return EVAL_BODY_BUFFERED;
    }


    public int doEndTag() throws JspTagException {
        return EVAL_PAGE;
    }

    /**
     * Set the value of the field.
     */
    public int doAfterBody() throws JspTagException {
        setFieldVar();

        String body = "";
        if (bodyContent != null) body = bodyContent.getString();
        // Get the new value from the body.
        if ((field != null) && (field.getType() == Field.TYPE_BYTE)) {
            // if the field type is a BYTE  thing, we expect a BASE64 encoded String...
            getNodeVar().setByteValue(fieldName, org.mmbase.util.Encode.decodeBytes("BASE64", body));
        } else {
            String newValue = convert(body);
            getNodeVar().setValue(fieldName, newValue);
            if (getId() != null) {
                getContextTag().register(getId(), newValue);
            }
        }
        findNodeProvider().setModified();

        return SKIP_BODY;
    }
}
