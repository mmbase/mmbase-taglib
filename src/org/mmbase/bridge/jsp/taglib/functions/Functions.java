/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.functions;


import org.mmbase.bridge.jsp.taglib.ContentTag;
import java.util.Collection;
import java.util.Iterator;

import org.mmbase.bridge.Node;
import org.mmbase.bridge.NodeList;
import org.mmbase.util.Casting;

/**
 * Functions for EL variables, and XSL.
 * Like this:

<mm:import id="nodelist" vartype="list">1,2,123</mm:import>
<mm:cloud>
  <mm:node number="124" id="node" />
  <c:choose>
    <c:when test="${mm:contains(nodelist, node)}">
      YES!
    </c:when>
    <c:otherwise>
      NO!
    </c:otherwise>
  </c:choose>
</mm:cloud>
 * @author  Michiel Meeuwissen
 * @since   MMBase-1.8
 * @version $Id: Functions.java,v 1.9 2005-05-09 10:54:36 michiel Exp $
 * @todo    EXPERIMENTAL
 */
public class Functions {

    /**
     * MMBase specific 'contains' (for Collections). For strings use fn:contains.
     */
    public static boolean contains(Collection col, Object obj) {
        if (col == null) return false;
        if (obj instanceof Node) {
            if (col instanceof NodeList) {
                if (col.contains(obj)) return true;
            } else {
                obj = new Integer(((Node) obj).getNumber());
            }
        }
        if (col.contains(obj)) return true;
        return col.contains(Casting.toString(obj));
    }

    /**
     * MMBase specific 'remove' (for Collections).
     */
    public static void remove(Collection col, Object obj) {
        if (obj instanceof Collection) { // like removeAll
            Iterator i = ((Collection) obj).iterator();
            while (i.hasNext()) {
                remove(col, i.next());
            }
        } else {
            if (obj instanceof Node) {
                col.remove(new Integer(((Node) obj).getNumber()));
            }
            col.remove(Casting.toString(obj));
            col.remove(obj);
        }
    }


    /**
     * Provides the 'escape' functionality to the XSLT itself. (using taglib:escape('p', mytag))
     * 
     * @since MMBase-1.8
     */
    public static String escape(String escaper, String string) {
        try {
            return ContentTag.getCharTransformer(escaper, null, null).transform("" + Casting.unWrap(string));
        } catch (Exception e) {
            return "Could not escape " + string + " with escape " + escaper + " : " + e.getMessage();
        }
    }



}
