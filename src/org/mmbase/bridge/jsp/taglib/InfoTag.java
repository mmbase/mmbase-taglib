/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;
import  org.mmbase.bridge.jsp.taglib.util.Attribute;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspException;

/**
 * Calls 'doInfo' from NodeManager or from Module.
 *
 * @author Michiel Meeuwissen
 * @version $Id: InfoTag.java,v 1.22 2003-11-19 16:57:42 michiel Exp $ 
 * @deprecated
 */

public class InfoTag extends  CloudReferrerTag implements Writer {

    private Attribute nodeManager = Attribute.NULL;
    private Attribute module      = Attribute.NULL;
    private Attribute command     = Attribute.NULL;

    public void setNodemanager(String nm) throws JspTagException {
        nodeManager = getAttribute(nm);
    }
    public void setModule(String m) throws JspTagException {
        module = getAttribute(m);
    }
    public void setCommand(String c) throws JspTagException {
        command = getAttribute(c);
    }

    public int doStartTag() throws JspTagException {
        String result;
        if (nodeManager != Attribute.NULL) {
            if (module != Attribute.NULL) {
                throw new JspTagException("Cannot give both module and nodemanager");
            }
            result = getCloud().getNodeManager(nodeManager.getString(this)).getInfo(command.getString(this),
                                                                                    pageContext.getRequest(),
                                                                                    pageContext.getResponse());
        } else if (module != Attribute.NULL) {
            result = getCloudContext().getModule(module.getString(this)).getInfo(command.getString(this),
                                                                                 pageContext.getRequest(),
                                                                                 pageContext.getResponse());
        } else {
            throw new JspTagException("Must give module or nodemanager");
        }

        
        helper.setValue(result);
        if (getId() != null) {
            getContextProvider().getContextContainer().register(getId(), helper.getValue());
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
