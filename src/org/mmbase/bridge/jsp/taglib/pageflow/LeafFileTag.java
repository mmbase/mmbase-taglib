/*
 
This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.
 
The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license
 
 */
package org.mmbase.bridge.jsp.taglib.pageflow;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import javax.servlet.jsp.JspTagException;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * Like IncludeTag, but an entire tree of files is being probed to find the one
 * that has the most specified value.
 *
 * This is a taglib-implementation of the 'LEAFFILE' command.
 * A full description of this command can be found in the mmbase-taglib.xml file.
 *
 * Note that the interesting functionality is implemented in the 'TreeHelper' class.
 * @author Johannes Verelst
 * @version $Id: LeafFileTag.java,v 1.12 2004-07-26 20:17:59 nico Exp $
 */

public class LeafFileTag extends UrlTag {
    
    private static final Logger log = Logging.getLoggerInstance(LeafFileTag.class);
    protected Attribute  objectList = Attribute.NULL;
    protected TreeHelper th = new TreeHelper();


    public int doStartTag() throws JspTagException {
        if (page == Attribute.NULL) {
            throw new JspTagException("Attribute 'page' was not specified");
        }
        if (objectList == Attribute.NULL) {
            throw new JspTagException("Attribute 'objectlist' was not specified");
        }        
        return super.doStartTag();
    }    
    protected String getPage() throws JspTagException {
        String orgPage  = super.getPage();
        String leafPage = th.findLeafFile(orgPage, objectList.getString(this), pageContext.getSession());
        if (log.isDebugEnabled()) {
            log.debug("Retrieving page '" + leafPage + "'");
        }
        if (leafPage == null) throw new JspTagException("Could not find page " + orgPage);
        return leafPage;
    }

    public int doEndTag() throws JspTagException {
        th.setCloud(getCloudVar());        
        return super.doEndTag();
    }
    
    public void setObjectlist(String p) throws JspTagException {
        objectList = getAttribute(p);
    }
    
    // override to cancel 
    protected boolean doMakeRelative() {
    	log.debug("doMakeRelative() overridden!");
        return false;
    }
}
