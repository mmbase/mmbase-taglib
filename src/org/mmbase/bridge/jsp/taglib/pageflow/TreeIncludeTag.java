/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

 */
package org.mmbase.bridge.jsp.taglib.pageflow;

import java.util.ArrayList;
import java.util.Map;

import org.mmbase.bridge.jsp.taglib.TaglibException;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import javax.servlet.jsp.JspTagException;

import org.mmbase.util.Casting;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * Like IncludeTag, but an entire tree of files is being probed to find the one
 * that has the most specified value.
 *
 * This is a taglib-implementation of the 'TREEPART' command, but renamed to
 * 'Treeinclude' for esthetic reasons :)
 * A full description of this command can be found in the mmbase-taglib.xml file.
 *
 * @author Johannes Verelst
 * @version $Id: TreeIncludeTag.java,v 1.23 2007-06-21 15:50:20 nklasens Exp $
 */

public class TreeIncludeTag extends IncludeTag {

    private static final Logger log = Logging.getLoggerInstance(TreeIncludeTag.class);
    protected Attribute objectList = Attribute.NULL;

    public void setObjectlist(String p) throws JspTagException {
        objectList = getAttribute(p);
    }

    private TreeHelper th = new TreeHelper();

    protected String getPage(String p) throws JspTagException {
        try {
            return th.findTreeFile(p, objectList.getValue(this).toString(),
                                   pageContext.getSession());
        } catch (java.io.IOException ioe) {
            throw new TaglibException(ioe);
        }
    }

    public void initTag() throws JspTagException {
        super.initTag(false);
        th.setCloud(getCloudVar());
        th.setBackwardsCompatible(! "false".equals(pageContext.getServletContext().getInitParameter("mmbase.taglib.smartpath_backwards_compatible")));
        url.setLegacy();
        if (log.isDebugEnabled()) {
            log.debug("leaffile end of starttag: " + url.toString());
        }
    }

    public void doFinally() {
        th.doFinally();
        super.doFinally();
    }


    // override to cancel
    protected boolean doMakeRelative() {
        log.debug("doMakeRelative() overridden!");
        return false;
    }
}
