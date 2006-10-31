/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

 */
package org.mmbase.bridge.jsp.taglib.pageflow;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.jsp.taglib.util.Notfound;
import javax.servlet.jsp.JspTagException;
import javax.servlet.http.HttpServletRequest;

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
 * @version $Id: LeafFileTag.java,v 1.17 2006-10-31 15:32:56 michiel Exp $
 */

public class LeafFileTag extends UrlTag {

    private static final Logger log = Logging.getLoggerInstance(LeafFileTag.class);
    protected Attribute  objectList = Attribute.NULL;
    protected TreeHelper th = new TreeHelper();

    protected Attribute notFound        = Attribute.NULL;

    public void setNotfound(String n) throws JspTagException {
        notFound = getAttribute(n);
    }

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

        if (leafPage == null || "".equals(leafPage)) {
            throw new JspTagException("Could not find page " + orgPage);
        }
        StringBuilder show = new StringBuilder();
        if (! useAbsoluteAttribute(show, leafPage)) {
            if (leafPage.charAt(0) == '/') {
                HttpServletRequest req =  (HttpServletRequest) getPageContext().getRequest();
                show.insert(0, req.getContextPath());
            }
            show.append(leafPage);
        }

        return show.toString();

    }

    public int doEndTag() throws JspTagException {
        th.setCloud(getCloudVar());
        int retval = super.doEndTag();
        return retval;
    }

    public void doFinally() {
        th.doFinally();
        super.doFinally();
    }

    public void setObjectlist(String p) throws JspTagException {
        objectList = getAttribute(p);
    }

    protected String getUrl(boolean writeamp, boolean encode) throws JspTagException {
        String url = "";
        try {
            url = super.getLegacyUrl(writeamp, encode);
        } catch (JspTagException e) {
            // I think this does not happen
            if (Notfound.get(notFound, this) == Notfound.SKIP) {
                throw e;
            }
        }
        return url;
    }

    // override to cancel
    protected boolean doMakeRelative() {
    	log.debug("doMakeRelative() overridden!");
        return false;
    }
}
