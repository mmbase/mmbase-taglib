/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.util;

import javax.servlet.jsp.PageContext;
import java.util.Map;

/**
 * This ContextContainer provides its own 'backing', it is used as 'subcontext' in other contextes.
 *
 * @author Michiel Meeuwissen
 * @version $Id: StandaloneContextContainer.java,v 1.9 2005-05-18 08:08:09 michiel Exp $
 * @since MMBase-1.8
 **/

public class StandaloneContextContainer extends ContextContainer {


    /**
     * A simple map, which besides to itself also registers to page-context.
     */
    protected BasicBacking backing;
        
    /**
     * Since a ContextContainer can contain other ContextContainer, it
     * has to know which ContextContainer contains this. And it also
     * has an id.
     */
    public StandaloneContextContainer(PageContext pc, String i, ContextContainer p) {
        super(pc, i, p);
        backing = new BasicBacking(pc);
        // values must fall through to PageContext, otherwise you always must prefix by context, even in it.
    }


    protected  Backing getBacking() {
        return backing;        
    }

    public void release() {
        backing.release();
    }



    protected boolean checkJspVar(String jspvar, String id) {
        return true;
    }
    
}
