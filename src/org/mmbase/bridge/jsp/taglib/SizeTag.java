/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspException;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;


/**
 * The size of a list.
 *
 *
 * @author Michiel Meeuwissen
 *
 */

public class SizeTag extends ListReferrerTag implements Writer {

    private static Logger log = Logging.getLoggerInstance(SizeTag.class.getName());
    // Writer implementation:
    protected WriterHelper helper = new WriterHelper();
    public void setVartype(String t) throws JspTagException {
        helper.setVartype(t);
    }
    public void setJspvar(String j) {
        helper.setJspvar(j);
    }
    public void setWrite(String w) throws JspTagException {
        helper.setWrite(getAttribute(w));
    }
    public Object getWriterValue() {
        return helper.getValue();
    }
    public void haveBody() { helper.haveBody(); }


    public int doStartTag() throws JspTagException{
        helper.setTag(this);
        helper.setValue(new Integer(getList().size()));

        if (getId() != null) {
            getContextTag().register(getId(), helper.getValue());
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doAfterBody() throws JspException {
        return helper.doAfterBody();
    }

    /**
     *
     **/
    public int doEndTag() throws JspTagException {
        return helper.doEndTag();
    }

}
