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
import org.mmbase.bridge.jsp.taglib.WriterHelper;
import org.mmbase.util.logging.*;


/**
 * A basic implementation for the backing of a ContextContainter. It uses a HashMap, but is also
 * writes every entry to the temporary to the page-context, to make them available to JSP2's
 * expression language, unless the 'ELIgnored' parameter of the MMBase taglib is true, or no
 * pageContext is given in the constructor.

 * @author Michiel Meeuwissen
 * @since MMBase-1.8
 * @version $Id: BasicBacking.java,v 1.8 2006-11-21 14:01:15 michiel Exp $
 */

public  class BasicBacking extends AbstractMap<String, Object>  implements Backing {
    private static final Logger log = Logging.getLoggerInstance(BasicBacking.class);

    private static final String PAGECONTEXT_KEY = "org.mmbase.taglib.basicbacking$";

    private static int uniqueNumbers = 0;
    private static final int SCOPE = PageContext.PAGE_SCOPE;

    private int uniqueNumber = ++uniqueNumbers;

    protected Map<String, Object> originalPageContextValues;
    private final Map<String, Object> b = new HashMap<String, Object>(); // the actual backing.

    private boolean isELIgnored;
    private  PageContext pageContext;

    /**
     * @param pc The page-context to which variables must be reflected or <code>null</code> if this must not happen.
     */
    public BasicBacking(PageContext pc) {
        pageContext = pc;
        if (log.isDebugEnabled()) {
            log.debug("Instantiating " + uniqueNumber + "  with pc " + pc + "  "  + Logging.stackTrace(12));
        }
        isELIgnored = pc == null || "true".equals(pageContext.getServletContext().getInitParameter(ContextTag.ISELIGNORED_PARAM));
        if (! isELIgnored) {
            originalPageContextValues = new HashMap<String, Object>();
            pageContext.setAttribute(PAGECONTEXT_KEY + uniqueNumber, originalPageContextValues);
        } else {
            originalPageContextValues = null;
            if (log.isDebugEnabled()) {
                if (pc == null) {
                    log.debug("ISELIGNORED because pc == null");
                } else {
                    log.debug("ISELIGNORED because setting");
                }
            }
        }

    }

    public void pushPageContext(PageContext pc) {
        isELIgnored = pc == null || "true".equals(pc.getServletContext().getInitParameter(ContextTag.ISELIGNORED_PARAM));
        if (log.isDebugEnabled()) {
            log.debug("Pushing page Context " + pc + " --> " + isELIgnored);
        }
        if (isELIgnored) return; // never mind

        PageContext origPageContext = pageContext;
        pageContext = pc;
        originalPageContextValues = (Map<String, Object>) pageContext.getAttribute(PAGECONTEXT_KEY + uniqueNumber);
        if (originalPageContextValues == null) {
            originalPageContextValues = new HashMap<String, Object>();
            pageContext.setAttribute(PAGECONTEXT_KEY + uniqueNumber, originalPageContextValues);
        }
        if (! origPageContext.equals(pageContext)) {
            for (Map.Entry<String, Object> entry : b.entrySet()) {
                mirrorPut(entry.getKey(), entry.getValue());
            }
        }
    }
    public void pullPageContext(PageContext pc) {
        if (isELIgnored || pc == null) return;
        pageContext = pc;
        originalPageContextValues = (Map<String, Object>) pageContext.getAttribute(PAGECONTEXT_KEY + uniqueNumber);
    }

    public void setJspVar(PageContext pc, String jspvar, int vartype, Object value) {
        if (jspvar == null) return;
        if (value == null) return;
        // When it doesn't, it goes ok. (at least I think that this is the difference between orion and tomcat)
        if (vartype == WriterHelper.TYPE_STRING) {
            // string is final, the wrapped version cannot be string..
            Object v = Casting.unWrap(value);
            if (v == null) return;
            pc.setAttribute(jspvar, v);
        } else {
            pc.setAttribute(jspvar, value);
        }

    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return new AbstractSet<Map.Entry<String, Object>>() {
                public int size() {
                    return b.size();
                }
                public Iterator<Map.Entry<String, Object>> iterator() {
                    return new Iterator<Map.Entry<String, Object>>() {
                        Iterator<Map.Entry<String, Object>> i = b.entrySet().iterator();
                        Map.Entry<String, Object> last = null;
                        public boolean hasNext() {
                            return i.hasNext();
                        }
                        public Map.Entry<String, Object> next() {
                            last = new Map.Entry<String, Object>() {
                                final Map.Entry<String, Object> wrapped = i.next();
                                public final String getKey() {
                                    return wrapped.getKey();
                                }
                                public final Object getValue() {
                                    return wrapped.getValue();
                                }

                                public final Object setValue(Object v) {
                                    BasicBacking.this.mirrorPut(wrapped.getKey(), v);
                                    return wrapped.setValue(v);
                                }

                            };
                            return last;
                        }
                        public void remove() {
                            i.remove();
                            if (! isELIgnored) {
                                String key = last.getKey();
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

    protected void mirrorPut(String key, Object value) {
        log.debug("Mirror putting " + key + "=" + value + " in a " + getClass() + "( " + uniqueNumber + ") with  pageContext " + pageContext);
        if (isELIgnored) {
            log.debug("EL IGNORED!");
            return;
        }

        if (! originalPageContextValues.containsKey(key)) {
            // log.debug("Storing pageContext key " + key);
            originalPageContextValues.put( key, pageContext.getAttribute(key, SCOPE));
        }
        if (value != null) {
            pageContext.setAttribute(key, Casting.wrap(value, (CharTransformer) pageContext.findAttribute(ContentTag.ESCAPER_KEY)), SCOPE);
        } else {
            pageContext.removeAttribute(key, SCOPE);
        }
    }
    public Object put(String key, Object value) {
        mirrorPut(key, value);
        return b.put(key, value);
    }

    // overriden for efficiency only (the implementation of AbstractMap does not seem very efficient)
    public Object get(String key) {
        return b.get(key);
    }

    public Object getOriginal(String key) {
        return b.get(key);
    }
    public boolean containsOwnKey(String key) {
        return b.containsKey(key);
    }

    void release() {
        if (originalPageContextValues != null) {
            //log.debug("Restoring pageContext with " + originalPageContextValues);
            // restore the pageContext
            for (Map.Entry<String, Object> e : originalPageContextValues.entrySet()) {
                if (e.getValue() == null) {
                    pageContext.removeAttribute(e.getKey(), SCOPE);
                } else {
                    pageContext.setAttribute(e.getKey(), e.getValue(), SCOPE);
                }
            }
            originalPageContextValues.clear();
        }
    }

    public String toString() {
        return "BASIC BACKING " + super.toString();
    }

} 
