
/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.pageflow;


import javax.servlet.jsp.JspTagException;
import org.mmbase.bridge.jsp.taglib.TaglibException;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;


/**
 * Does a redirect, using the features of UrlTag.
 *
 * @author Michiel Meeuwissen
 * @version $Id: RedirectTag.java,v 1.1 2003-09-05 16:32:37 michiel Exp $
 * @since MMBase-1.7
 */

public class RedirectTag extends UrlTag  {

    private static final Logger log = Logging.getLoggerInstance(RedirectTag.class); 

    public int doAfterBody() throws JspTagException {
        try {
            // dont set value, but redirect.
            javax.servlet.http.HttpServletResponse response = (javax.servlet.http.HttpServletResponse) pageContext.getResponse();
            String url = response.encodeRedirectURL(getUrl(false, false));
            log.info("Redirecting to " + url);
            response.sendRedirect(url);
        } catch (java.io.IOException io) {
            throw new TaglibException(io);
        }
        return SKIP_BODY;
    }


    /**
     * Method called at end of Tag used to send redirect,
     * always skips the remainder of the JSP page.
     *
     * @return SKIP_PAGE
     */ 
    public final int doEndTag() throws JspTagException {
        try {
            // dont set value, but redirect.
            javax.servlet.http.HttpServletResponse response = (javax.servlet.http.HttpServletResponse) pageContext.getResponse();
            String url = response.encodeRedirectURL(getUrl(false, false));
            log.info("Redirecting to " + url);
            response.sendRedirect(url);
        } catch (java.io.IOException io) {
            throw new TaglibException(io);
        }
	return SKIP_PAGE;
    }




}