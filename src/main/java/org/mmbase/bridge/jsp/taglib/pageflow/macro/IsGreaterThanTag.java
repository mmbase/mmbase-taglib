/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.pageflow.macro;

import org.mmbase.bridge.jsp.taglib.pageflow.CompareTag;

/**
 * Checks if a value of a context variable is greater than a given value
 * 
 * @author Michiel Meeuwissen
 * @version $Id$
 */

public class IsGreaterThanTag extends CompareTag  {
    @Override
    protected boolean doCompare(Comparable<Comparable> compare1, Comparable compare2) {
        return compare1.compareTo(compare2) > 0;
    }
}
