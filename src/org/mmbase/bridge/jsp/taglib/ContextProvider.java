/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;
import javax.servlet.jsp.JspTagException;
import org.mmbase.bridge.jsp.taglib.util.ContextContainer;


/**
 * Abstract representation of a 'context' tag. The one context is of
 * course the context tag itself. But also List tags work as a kind of
 * contextes.
 * 
 * @since MMBase-1.7
 * @author Michiel Meeuwissen
 * @version $Id: ContextProvider.java,v 1.3 2003-06-06 10:03:06 pierre Exp $
 **/

public interface ContextProvider  extends TagIdentifier {

    public ContextContainer getContainer() throws JspTagException;

}