/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.util;

import org.mmbase.bridge.jsp.taglib.*;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.*;

import java.util.*;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * This is a HashMap, but the keys can contain 'dots', in which case
 * there is searched for HashMaps in the HashMap.
 *
 * @author Michiel Meeuwissen
 * @version $Id: ContextContainer.java,v 1.14 2003-09-05 16:32:38 michiel Exp $
 **/

public class ContextContainer extends HashMap {
    private static final Logger log = Logging.getLoggerInstance(ContextContainer.class);

    public static final int LOCATION_NOTSET         = -10;
    public static final int LOCATION_PAGE           = 0;
    public static final int LOCATION_PARENT         = 5; // uses parent Contex, if there is one.
    public static final int LOCATION_PARAMETERS     = 10;
    public static final int LOCATION_MULTIPART      = 20;
    public static final int LOCATION_SESSION        = 30;
    public static final int LOCATION_COOKIE         = 40;
    public static final int LOCATION_ATTRIBUTES     = 50;

    public static int stringToLocation(String s) throws JspTagException {
        int location;
        if ("parent".equalsIgnoreCase(s)) {
            location = LOCATION_PARENT;
        } else if ("page".equalsIgnoreCase(s)) {
            location = LOCATION_PAGE;
        } else if ("session".equalsIgnoreCase(s)) {
            location = LOCATION_SESSION;
        } else if ("parameters".equalsIgnoreCase(s)) {
            location = LOCATION_PARAMETERS;
        } else if ("parameter".equalsIgnoreCase(s)) {
            location = LOCATION_PARAMETERS;
        } else if ("postparameters".equalsIgnoreCase(s)) { // backward compatible
            location = LOCATION_MULTIPART;
        } else if ("multipart".equalsIgnoreCase(s)) {
            location = LOCATION_MULTIPART;
        } else if ("cookie".equalsIgnoreCase(s)) {
            location = LOCATION_COOKIE;
        } else if ("attributes".equalsIgnoreCase(s)) {
            location = LOCATION_ATTRIBUTES;
        } else {
            throw new JspTagException("Unknown location '" + s + "'");
        }
        return location;
    }

    public static String locationToString(int i) {
        switch(i) {
        case LOCATION_PARENT:      return "parent";
        case LOCATION_PARAMETERS:  return "parameters";
        case LOCATION_SESSION:     return "session";
        case LOCATION_PAGE:        return "page";
        case LOCATION_MULTIPART:   return "multipart";
        case LOCATION_COOKIE:      return "cookie";
        case LOCATION_ATTRIBUTES:  return "attributes";
        default:                   return "<>";
        }
    }



    private static boolean isContextVarNameChar(char c) {
        return Character.isLetter(c) || Character.isDigit(c) || c == '_';
    }
    static boolean isContextIdentifierChar(char c) {
        return isContextVarNameChar(c) || c == '.' || c =='/'; // / for forward compatibility?
    }


    private String id;
    protected ContextContainer parent;

    /**
     * Since a ContextContainer can contain other ContextContainer, it
     * has to know which ContextContainer contains this. And it also
     * has an id.
     */

    public ContextContainer(String i, ContextContainer p) {
        super();
        id = i;
        parent = p;
    }


    public String getId() {
        return id;
    }

    /**
     * @since MMBase-1.7
     */
    ContextContainer getParent() {        
        return parent;
    }

    /**
     * Keys must be Strings, so put(Object, ..) is forbidden in this HashMap!
     */

    public Object put(Object key, Object value) {
        if (key instanceof String) {
            try {
                return put((String) key, value);
            } catch (JspTagException e) {
                throw new RuntimeException(e.toString());
            }
        } else {
            throw new RuntimeException("Error, key should be string in ContextContainers! (Tried " + key.getClass().getName() + ")");
        }
    }
        
    /**
     * Not all Strings can be allowed as keys. Keys are like variable names.
     */

    public Object put(String key, Object value) throws JspTagException {
        if (key.indexOf('.') != -1) {
            throw new JspTagException("Key may not contain dots (" + key + ")");
        }
        return super.put(key, value);
    }
    public boolean containsKey(Object key) {
        throw new RuntimeException("Error, key should be string in ContextContainers!");
    }

    /**
     * This function takes a key, which can contain dots. It returns
     * a structure with a new ContextContainer and the rest of the
     * key. It also returns if it had to 'go down' to find this,
     * because if it has done this, the next time you may not go up
     * again (check the parent).
     *
     */

    private Pair getPair(String key, boolean checkParent) throws JspTagException {
        // checking if key contains a 'dot'.
        int dotpos = key.indexOf('.');
        if (dotpos > 0) {
            String contextKey = key.substring(0, dotpos);
            // find the Context:
            boolean wentDown = true;
            Object c = simpleGet(contextKey, checkParent);
            if(c == null && checkParent && parent != null) {
                c =  parent.get(key, true);
                wentDown = false;
            }

            if(c == null) {
                throw new JspTagException("Context '" + contextKey+ "' could not be found.");
            }
            if (! (c instanceof ContextContainer)) {
                throw new JspTagException(contextKey + " is not a Context, but a " + c.getClass().getName());
            }
            String newKey = key.substring(dotpos + 1);
            // and search with that one:
            return new Pair((ContextContainer)c, newKey, wentDown);
        } else {
            return null;
        }

    }
    /**
     * Like containsKey but doesn't check for dots.
     */
    private boolean simpleContainsKey(String key, boolean checkParent) {
        boolean result = super.containsKey(key);
        if (result == false && checkParent && parent != null) {
            result = parent.simpleContainsKey(key, true);
        }
        return result;
    }

    /**
     * Like the containsKey of HashMap.
     *
     * @param key          The key to search
     * @param checkParent  If this is false, it will only look in the current Container (and below).
     */

    public boolean containsKey(String key, boolean checkParent) throws JspTagException {
        Pair p = getPair(key, checkParent);
        if (p == null) {
            return simpleContainsKey(key, checkParent);
        } else {
            return p.context.containsKey(p.restKey, ! p.wentDown);
        }
    }

    public boolean containsKey(String key) throws JspTagException {
        return containsKey(key, true);
    }

    /**
     * Like get, but does not try to search dots, because you know already that there aren't.
     */
    private Object simpleGet(String key, boolean checkParent) { // already sure that there is no dot.
        Object result =  super.get(key);
        if (result == null && checkParent && parent != null) {
            return parent.simpleGet(key, true);
        }
        return result;
    }

    /**
     * Like get, but you can explicity indicate if to search 'parent' Contextes as well.
     */

    public Object get(String key, boolean checkParent) throws JspTagException {
        Pair p = getPair(key, checkParent);
        if (p == null) {
            return simpleGet(key, checkParent);
        } else {
            return p.context.get(p.restKey, ! p.wentDown);
        }
    }

    /**
     *
     */

    public Object get(String key) throws JspTagException {
        return get(key, true);
    }

    public Set keySet() {
        HashSet result = new HashSet(super.keySet());
        if (parent != null) {
            result.addAll(parent.keySet());
        }
        return result;
    }

    /**
     * @since MMBase-1.7
     */
    Set keySet(boolean checkParent) {
        if (checkParent) {
            return keySet();
        } else {
            return super.keySet();
        }
    }

    // utilities, since MMBase-1.7 moved from ContextTag to here.

    /**
     * @since MMBase-1.7 (here)
     */
    public Object getObject(String key) throws JspTagException {
        if (! containsKey(key, true)) { // do check parent.
            throw new JspTagException("Object '" + key + "' is not registered. Registered are " + keySet());
        }
        if (log.isDebugEnabled()) {
            log.debug("Getting '" + key + "' from container " + this);
        }
        return get(key);
    }

    /**
     * @since MMBase-1.7 (here)
     */
    public void register(String newid, Object n, boolean check) throws JspTagException {
        if (log.isDebugEnabled()) {
            log.trace("registering " + n + " a (" + (n!=null ? n.getClass().getName() :"")+ ") under " + newid + " with context " + id);
        }
        // Check if the id is a valid identifier
        // A valid id must begin with a letter or underscore, followed
        // by letters, underscores and digits.
        boolean valid = true;
        char chars[] = newid.toCharArray();
        if (chars.length < 1) {
            log.debug("Id must be longer then 0");
            valid = false;
        } else {
            if (Character.isLetter(chars[0]) || chars[0] == '_') {
                if (log.isDebugEnabled()) log.debug("First character is valid, checking the rest of " + newid);
                for (int i = 1; i < chars.length; ++i) {
                    if (! isContextVarNameChar(chars[i])) {
                        valid = false;
                        break;
                    }
                }
            } else {
                if (log.isDebugEnabled()) log.debug("First character is not valid: " + chars[0]);
                valid = false;
            }
        }
        if (! valid) {
            log.info(Logging.stackTrace(new Throwable()));
            throw new TaglibException ("'" + newid + "' is not a valid Context identifier", new Throwable());
        }

        log.debug("Valid");
        //pageContext.setAttribute(id, n);
        if (check && isRegistered(newid)) {
            JspTagException e;
	    if(id == null) {
		e = new JspTagException("Object with id " + newid + " was already registered in the context without id (root?).");
	    } else {
		e = new JspTagException("Object with id " + newid + " was already registered in Context '" + id  + "'.");
	    }
            if (log.isDebugEnabled()) {
                log.debug(Logging.stackTrace(e));
            }
            throw e;
        }
        if (log.isDebugEnabled()) {
            log.debug("putting '" + newid + "'/'" + n + "' in " + this);
        }
        put(newid, n);
    }

    /**
     * @since MMBase-1.7 (here)
     */
    public void register(String newid, Object n) throws JspTagException {
        register(newid, n, true);
    }

    /**
     * @since MMBase-1.7
     */
    void registerAll(Map map) throws JspTagException {
        if (map == null) return;
        Iterator i = map.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            register(key, entry.getValue());
        }
        
    }
    /**
     * @since MMBase-1.7
     */
    void unRegisterAll(Set set) throws JspTagException {
        if (set == null) return;
        Iterator i = set.iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            unRegister(key);
        }
     
    }
    /**
     * @since MMBase-1.7 (here)
     */
    public void registerNode(String newid,  org.mmbase.bridge.Node n) throws JspTagException {
        register(newid, n);
    }
    /**
     * @since MMBase-1.7 (here)
     */
    public boolean isRegistered(String key) throws JspTagException {
        return containsKey(key, false); // don't check parent.
    }
    /**
     * @since MMBase-1.7 (here)
     */
    public void unRegister(String key) throws JspTagException {
        if (log.isDebugEnabled()) log.debug("removing object '" + key + "' from Context '" + id + "'");
        remove(key);
    }
    /**
     * @since MMBase-1.7 (here)
    */
    public void reregister(String id, Object n) throws JspTagException {
        unRegister(id);
        register(id, n);
    }

   
    /**
     * @since MMBase-1.7
     */

    public Object find(PageContext pageContext, int from, String referid) throws JspTagException {
        Object result = null;
        switch (from) {
        case LOCATION_COOKIE:
            javax.servlet.http.Cookie[] cookies = ((HttpServletRequest) pageContext.getRequest()).getCookies();
            if (cookies == null) {
                log.debug("Cannot use cookies");
            } else {
                log.debug("Found cookies");
                for (int i=0; i< cookies.length; i++) {
                    if (log.isDebugEnabled()) {
                        log.debug(cookies[i].getName() + "/" + cookies[i].getValue());
                    }
                    if (cookies[i].getName().equals(referid)) {
                        // simply return the first value found.
                        // this is probably a little to simple...
                        // since a cookie can e.g. also have another path.
                        result = cookies[i].getValue();
                        break;
                    }
                }
            }
            break;
        case LOCATION_SESSION:
            if (((HttpServletRequest) pageContext.getRequest()).getSession() == null) {
                throw new JspTagException("Cannot use session if session is disabled");
            }
            result = ((HttpServletRequest) pageContext.getRequest()).getSession().getAttribute(referid);
            break;
        case LOCATION_MULTIPART:
            if (MultiPart.isMultipart(pageContext)) {
                if (log.isDebugEnabled()) {
                    log.debug("searching " + referid + " in multipart post");
                }
                result = MultiPart.getMultipartRequest(pageContext).getParameterValues(referid);
            } else {
                throw new JspTagException("Trying to read from multipart post, while request was not a multipart post");
            }
            break;
        case LOCATION_PARAMETERS: {
            if (log.isDebugEnabled()) {
                log.debug("searching parameter " + referid);
            }
            HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
            String[] resultvec = req.getParameterValues(referid);
            if (resultvec != null) {
                if (log.isDebugEnabled()) log.debug("Found: " + resultvec);
                if (resultvec.length > 1) {
                    result = java.util.Arrays.asList(resultvec);
                } else {
                    String formEncoding = req.getCharacterEncoding();
                    if (log.isDebugEnabled()) log.debug("found encoding in the request: " + formEncoding);
                    if (formEncoding == null) {
                        // Java Servlet Specification Version 2.3 SRV.4.9
                        // says that a servlet engine should read a request
                        // as ISO-8859-1 if request.getCharacterEncoding()
                        // returns null. We override this behaviour because
                        // the browser propably sends the request in the
                        // same encoding as the html was send to te browser
                        // And it is likely that the html was send to the
                        // browser in the same encoding as the MMBase
                        // encoding property.
                        try {
                            result = new String(resultvec[0].getBytes("ISO-8859-1"), getDefaultCharacterEncoding(pageContext));
                        } catch (java.io.UnsupportedEncodingException e) {
                            throw new JspTagException("Unsupported Encoding: " + e.toString());
                        }
                    } else {
                        result = resultvec[0];
                    }
                }
            }
        }
        break;
        case LOCATION_PARENT:
            if (parent != null) {
                if (parent.isRegistered(referid)) {
                    result = parent.get(referid);
                    if (result == this) { // don't find this tag itself...
                        result = null;
                    }
                }
            }
            break;
        case LOCATION_PAGE:
            //result = pageContext.getAttribute(referid);
            break;
        case LOCATION_ATTRIBUTES:

            result = ((HttpServletRequest) pageContext.getRequest()).getAttribute(referid);
            break;
        default:
            result = null;
        }
        return result;
 
    }

    /**
     * @since MMBase-1.7
     */

    public Object find(PageContext pageContext, String externid) throws JspTagException {
        log.debug("searching in parent");
        Object result;
        result = find(pageContext, LOCATION_PARENT, externid);
        if (result != null) return result;
        log.debug("searching in parameters");
        result = find(pageContext, LOCATION_ATTRIBUTES, externid);
        if (result != null) return result;
        log.debug("searching in parameters");
        result = find(pageContext, LOCATION_PARAMETERS, externid);
        if (result != null) return result;
        if (MultiPart.isMultipart(pageContext)) {
            log.debug("searching in multipart post");
            result = find(pageContext, LOCATION_MULTIPART, externid);
            if (result != null) return result;
        }
        if (((HttpServletRequest) pageContext.getRequest()).getSession() != null) {
            log.debug("searching in session");
            result = find(pageContext, LOCATION_SESSION, externid);
        }
        return result;
    }

    /**
     * Searches a key in request, postparameters, session, parent
     * context and registers it in this one.
     *
     *  Returns null if it could not be found.
     */
    public Object findAndRegister(PageContext pageContext, int from, String referid, String newid) throws JspTagException {
        return findAndRegister(pageContext, from, referid, newid, true);
    }

    public Object findAndRegister(PageContext pageContext, int from, String referid, String newid, boolean check) throws JspTagException {
        if (newid == null) {
            throw new JspTagException("Cannot register with id is null");
        }
        if (referid == null) {
            throw new JspTagException("Cannot refer with id is null");
        }
        Object result = find(pageContext, from, referid);
        // if it cannot be found, then 'null' will be put in the hashmap ('not present')
  
        register(newid, result, check);
        if (log.isDebugEnabled()) {
            log.debug("found " + newid + " (" + result + ")");
        }
        return result;
    }

    public Object findAndRegister(PageContext pageContext, String externid, String newid) throws JspTagException {
        return findAndRegister(pageContext, externid, newid, true);
    }
    public Object findAndRegister(PageContext pageContext, String externid, String newid, boolean check) throws JspTagException {
        if (log.isDebugEnabled()) {
            log.debug("searching to register object " + externid + " in context " + getId() + " check: " + check);
        }
        if (check && isRegistered(newid)) {
	    String mes;
	    if(getId() == null) {
		mes = "Object with id " + newid + " was already registered in the root context.";
	    } else {
		mes = "Object with id " + newid + " was already registered in Context '" + getId()  + "'.";
	    }
            log.debug(mes);
            throw new JspTagException(mes);
        }
        // if (findAndRegister(LOCATION_PAGE, referid, id)) return true;
        Object result = find(pageContext, externid);
        register(newid, result, check);
        return result;
    }

    public Object findAndRegister(PageContext pageContext, String id) throws JspTagException {
        return findAndRegister(pageContext, id, id);
    }
    public String findAndRegisterString(PageContext pageContext, String id) throws JspTagException {
        return (String) findAndRegisterString(pageContext, id, true);
    }

    public String findAndRegisterString(PageContext pageContext, String id, boolean check) throws JspTagException {
        return (String) findAndRegister(pageContext, id, id, check);
    }

    public boolean isPresent(String key) throws JspTagException {
        //if (! isRegisteredSomewhere(key)) {
        //    log.warn("Checking presence of unregistered context variable " + key + " in context " + getId());
        //    return false;
        //}
        Object value = get(key);

        if (value == null) {
            return false;
        } else {
            if (value instanceof List) {
                if (((List) value).size() == 0) return false;
            }
            return true;
        }
    }



    static String getDefaultCharacterEncoding(PageContext pageContext) {
        String def = (String) pageContext.getAttribute(ContextTag.DEFAULTENCODING_KEY);
        if (def == null) return "UTF-8";
        return def;
    }


    /**
     * Container class, to store results of 'getPair' function.
     *
     */

    private class Pair {
        ContextContainer context;
        String           restKey;
        boolean          wentDown;
        Pair(ContextContainer c, String k, boolean w) {
            context = c; restKey = k; wentDown = w;
        }
    }

}
