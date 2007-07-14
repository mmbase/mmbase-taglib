/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import javax.servlet.jsp.JspTagException;
/**
 * ParamHandlers can have the &lt;mm:param&gt; tag as subtag
 *
 * @author Michiel Meeuwissen
 * @since MMBase-1.9
 * @see    org.mmbase.bridge.jsp.taglib.pageflow.UrlTag
 * @version $Id: FrameworkParamHandler.java,v 1.1 2007-07-14 09:26:49 michiel Exp $
 */

public interface FrameworkParamHandler {

    public void addFrameworkParameter(String key, Object value) throws JspTagException;
}
