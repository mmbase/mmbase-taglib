/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;
import javax.servlet.jsp.JspTagException;

/**
 *
 * @author Michiel Meeuwissen 
 **/

public interface WriterReferrer {

    /**
     * A WriterReferrer has a 'writer' attribute.
     */
    public void setWriter(String w) throws JspTagException;

    /**
     * Returns the parent writer;
     */
    public Writer findWriter() throws JspTagException;

}
