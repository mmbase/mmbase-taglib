/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import org.mmbase.bridge.Cloud;
import org.mmbase.bridge.CloudContext;
import org.mmbase.bridge.LocalContext;

import javax.servlet.jsp.JspTagException;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;


/**
 * If you want to have attributes which obtain the value from a
 *  parameter, extend from this.
 *
 * @author Michiel Meeuwissen 
 */

public abstract class ContextReferrerTag extends BodyTagSupport {

    private ContextTag contextTag;
    private String contextId = null;

    /**
     * Refer to a specific context.
     */

    public void setContext(String c) {
        contextId = c;
    }
    

    protected String getAttributeValue(String attribute) throws JspTagException {
        String attributeValue = attribute;
        if (attribute.startsWith("param:")) {
            String param = attribute.substring(6);
            attributeValue = pageContext.getRequest().getParameter(param);
            if (attributeValue == null) {
                throw new JspTagException("Parameter " + param + " could not be found");
            }
        } else if (attribute.startsWith("session:")){
            String param = attribute.substring(8);
            javax.servlet.http.HttpServletRequest req = (javax.servlet.http.HttpServletRequest) pageContext.getRequest();
            attributeValue = (String) req.getSession().getAttribute(param);
            if (attributeValue == null) {
                throw new JspTagException("Session attribute " + param + " could not be found");
            }
        } else if (attribute.startsWith("context:")) {
            String param = attribute.substring(8);
            attributeValue = getString(param);
            if (attributeValue == null) {
                throw new JspTagException("Context attribute " + param + " could not be found");
            }
        }

        return attributeValue;        
    }

    
    protected TagSupport findParentTag(String classname, String id) throws JspTagException {
        
        Class clazz ;
        try {
            clazz = Class.forName(classname);
        } catch (java.lang.ClassNotFoundException e) {
            throw new JspTagException ("Could not find " + classname + " class");  
        }

        TagSupport cTag = (TagSupport) findAncestorWithClass((Tag)this, clazz); 
        if (cTag == null) {
            throw new JspTagException ("Could not find parent");  
        }
        
        if (id != null) { // search further, if necessary
            while (cTag.getId() != id) {
                cTag = (TagSupport) findAncestorWithClass((Tag)cTag, clazz);
                if (cTag == null) {
                    throw new JspTagException ("Could not find parent Tag with id " + id);  
                }
            }
            
        }
        return cTag;

    }

    protected ContextTag findContext() throws JspTagException {
        return (ContextTag) findParentTag("org.mmbase.bridge.jsp.taglib.ContextTag", contextId);
    }

    protected Object getObject(String id) throws JspTagException {
        if (contextTag == null) {
            contextTag = findContext();
        }
        return contextTag.getObject(id);
    }
    protected String getString(String id) throws JspTagException {
        return (String) getObject(id);
    }


}
