/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.jsp.taglib.util;

import javax.servlet.jsp.PageContext;
import java.util.*;
import org.mmbase.util.Casting;
import org.mmbase.util.transformers.CharTransformer;
import org.mmbase.bridge.jsp.taglib.ContextTag;
import org.mmbase.bridge.jsp.taglib.ContentTag;


/**
 * A basic implementation for the backing of a ContextContainter. It uses a HashMap, but is also
 * writes every entry to the temporary to the page-context, to make them available to JSP2's
 * expression language, unless the 'ELIgnored' parameter of the MMBase taglib is true, or no
 * pageContext is given in the constructor.

 * @author Michiel Meeuwissen
 * @since MMBase-1.8
 * @version $Id: BasicBacking.java,v 1.1 2005-03-22 15:02:05 michiel Exp $
 */

public  class BasicBacking extends AbstractMap {

    private static final int SCOPE = PageContext.PAGE_SCOPE;
    protected final Map originalPageContextValues;
    private final Map b = new HashMap();
    private final boolean isELIgnored;
    private final PageContext pageContext;

    /**
     * @param pc The page-context to which variables must be reflected or <code>null</code> if this must not happen.
     */
    public BasicBacking(PageContext pc) {
        pageContext = pc;
        isELIgnored = pc == null || "true".equals(pageContext.getServletContext().getInitParameter(ContextTag.ISELIGNORED_PARAM));
        if (! isELIgnored) {
            originalPageContextValues = new HashMap();
        } else {
            originalPageContextValues = null;
        }
    }
    // contains the values originally in the pageContext, so that they can be restored.
    public Set entrySet() {
        return new AbstractSet() {
                public int size() {
                    return b.size();
                }
                public Iterator iterator() {
                    return new Iterator() {
                            Iterator i = b.entrySet().iterator();
                            Map.Entry last = null;
                            public boolean hasNext() {
                                return i.hasNext();
                            }
                            public Object next() {
                                last = (Map.Entry) i.next();
                                return last;
                            }
                            public void remove() {
                                i.remove();
                                if (! isELIgnored) {
                                    String key = (String) last.getKey();
                                    if (! originalPageContextValues.containsKey(key)) {
                                        originalPageContextValues.put(key, pageContext.getAttribute(key, SCOPE));
                                    }
                                    pageContext.removeAttribute(key);
                                }
                            }
                        };
                }
            };
    }
    public Object put(Object key, Object value) {
        if (! isELIgnored) {
            if (! originalPageContextValues.containsKey(key)) {
                // log.debug("Storing pageContext key " + key);
                originalPageContextValues.put((String) key, pageContext.getAttribute((String) key, SCOPE));
            }

            if (value != null) {
                pageContext.setAttribute((String) key, Casting.wrap(value, (CharTransformer) pageContext.getAttribute(ContentTag.ESCAPER_KEY)), SCOPE);
            } else {
                pageContext.removeAttribute((String) key, SCOPE);
            }
        }
        return b.put(key, value);
    }
        
    public void release() {
        if (originalPageContextValues != null) {
            //log.debug("Restoring pageContext with " + originalPageContextValues);
            // restore the pageContext
            Iterator i = originalPageContextValues.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                if (e.getValue() == null) {
                    pageContext.removeAttribute((String) e.getKey(), SCOPE);
                } else {
                    pageContext.setAttribute((String) e.getKey(), e.getValue(), SCOPE);
                }
            }
            originalPageContextValues.clear();
        }
    }
        
        
} 