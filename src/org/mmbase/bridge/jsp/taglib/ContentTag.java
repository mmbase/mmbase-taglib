/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.*;
import java.util.*;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.mmbase.util.transformers.*;
import org.mmbase.util.functions.Parameters;

import org.mmbase.util.*;
import org.mmbase.security.UserContext;

import org.xml.sax.InputSource;
import org.w3c.dom.Element;


import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * Provided environmental information to its body's tags.
 *
 * @author Michiel Meeuwissen
 * @since MMBase-1.7
 * @version $Id: ContentTag.java,v 1.40 2005-05-09 10:54:36 michiel Exp $
 **/

public class ContentTag extends LocaleTag  {
    private static  Logger log;


    private static final CharTransformer COPY = CopyCharTransformer.INSTANCE;

    private static final long DEFAULT_EXPIRE_TIME = 60; // one minute

    public static final String ESCAPER_KEY = "org.mmbase.bridge.jsp.taglib.escaper";

    static final ContentTag DEFAULT = new ContentTag() {
            public CharTransformer getWriteEscaper() { return COPY; } 
            public String  getType()    { return "text/html"; } 
            public String  getEncoding(){ return "ISO-8859-1"; } 
        };

    private static final Map defaultEscapers       = new HashMap(); // contenttype id -> chartransformer id
    private static final Map defaultPostProcessors = new HashMap(); // contenttype id -> chartransformer id
    private static final Map defaultEncodings      = new HashMap(); // contenttype id -> charset to be used in content-type header (defaults to UTF-8)

    private static final Map contentTypes          = new HashMap(); // contenttype id  -> contenttype

    private static final Map charTransformers        = new HashMap(); // chartransformer id -> chartransformer instance.
    private static final Map parameterizedCharTransformerFactories  = new HashMap(); // chartransformer id -> chartransformer factories.

    static {
        try {
            log = Logging.getLoggerInstance(ContentTag.class);
            org.mmbase.util.XMLEntityResolver.registerPublicID("-//MMBase//DTD taglibcontent 1.0//EN", "taglibcontent_1_0.dtd", ContentTag.class);
            ResourceWatcher watcher = new ResourceWatcher(ResourceLoader.getConfigurationRoot().getChildResourceLoader("taglib")) {
                    public void onChange(String resource) {
                        defaultEscapers.clear();
                        defaultPostProcessors.clear();
                        defaultEncodings.clear();
                        charTransformers.clear();
                        contentTypes.clear();
                        initialize(getResourceLoader(), resource);
                    }
                };
            watcher.add("content.xml");
            watcher.start();
            watcher.onChange("content.xml");
        } catch (Exception e) {
            log.error(e.toString());
        }
    }


    private static CharTransformer readCharTransformer(XMLBasicReader reader, Element parentElement, String id) {
        List result = new ArrayList();
        Enumeration e = reader.getChildElements(parentElement, "class");
        while (e.hasMoreElements()) {
            Element element = (Element) e.nextElement();
            String claz = reader.getElementValue(element);

            String config = element.getAttribute("config");
            boolean back = "true".equalsIgnoreCase(element.getAttribute("back"));

            CharTransformer ct = Transformers.getCharTransformer(claz, config, " escaper " + id, back);
            if (ct == null) continue;
            result.add(ct);
        }
        if (result.size() == 0) {
            return COPY;
        } else if (result.size() == 1) {
            return (CharTransformer) result.get(0);
        } else {
            ChainedCharTransformer cct = new ChainedCharTransformer();
            cct.addAll(result);
            return cct;
        }
    }

    private static ParameterizedTransformerFactory readTransformerFactory(XMLBasicReader reader, Element parentElement, String id) {
        Enumeration e = reader.getChildElements(parentElement, "class");
        Element element = (Element) e.nextElement();
        String claz = reader.getElementValue(element);        
        boolean back = "true".equalsIgnoreCase(element.getAttribute("back"));        
        return Transformers.getTransformerFactory(claz, " parameterizedescaper " + id, back);
    }
    /**
     * Initialize the write-escapers for MMBase taglib.
     */
    private static void initialize(ResourceLoader taglibLoader, String resource) {
        log.service("Reading taglib write-escapers");
        InputStream stream = ContentTag.class.getResourceAsStream("resources/taglibcontent.xml");
        if (stream != null) {
            log.info("Reading backwards compatible resource " + ContentTag.class.getName()+"/resources/taglibcontext.xml");
            InputSource escapersSource = new InputSource(stream);
            readXML(escapersSource);
        }
        List resources = taglibLoader.getResourceList(resource);
        log.info("Using " + resources);
        ListIterator i = resources.listIterator();
        while (i.hasNext()) i.next();
        while (i.hasPrevious()) {
            try {
                URL u = (URL) i.previous();
                log.info("Reading " + u);
                URLConnection con = u.openConnection();
                if (con.getDoInput()) {
                    InputSource source = new InputSource(con.getInputStream());
                    readXML(source);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }       
        
    }

    protected static void readXML(InputSource escapersSource) {
        
        XMLBasicReader reader  = new XMLBasicReader(escapersSource, ContentTag.class);
        Element root = reader.getElementByPath("taglibcontent");

        Enumeration e = reader.getChildElements(root, "escaper");
        while (e.hasMoreElements()) {
            Element element = (Element) e.nextElement();
            String id   = element.getAttribute("id");
            CharTransformer ct = readCharTransformer(reader, element, id);
            if (charTransformers.containsKey(id)) {
                log.warn("Replaced an escaper '" + id + "' : " + ct );
            } else {
                log.service("Found an escaper '" + id + "' : " + ct);
            }
            charTransformers.put(id, ct);
        }
        log.service("Reading content tag parameterizedescaperss");
        e = reader.getChildElements(root, "parameterizedescaper");
        while (e.hasMoreElements()) {
            Element element = (Element) e.nextElement();
            String id   = element.getAttribute("id");
            ParameterizedTransformerFactory fact = readTransformerFactory(reader, element, id);
            if (parameterizedCharTransformerFactories.containsKey(id)) {
                log.warn("Replaced an pescaper '" + id + "' : " + fact );
            } else {
                log.service("Found an pescaper '" + id + "' : " + fact);
            }
            parameterizedCharTransformerFactories.put(id, fact);
            try {
                CharTransformer ct = (CharTransformer) fact.createTransformer(fact.createParameters());
                log.service("Could be instantiated with default parameters too");
                charTransformers.put(id, ct);
            } catch (Exception ex) {
                log.service("Could not be instantiated with default parameters only: " + ex.getMessage());
            }

        }
        log.service("Reading content tag post-processors");
        e = reader.getChildElements(root, "postprocessor");
        while (e.hasMoreElements()) {
            Element element = (Element) e.nextElement();
            String id   = element.getAttribute("id");
            CharTransformer ct = readCharTransformer(reader, element, id);
            if (charTransformers.containsKey(id)) {
                log.warn("Replaced an postprocessor '" + id + "' : " + ct);
            } else {
                log.service("Found an postprocessor '" + id + "' : " + ct);
            }
            charTransformers.put(id, ct);
        }

        e = reader.getChildElements(root, "content");
        while (e.hasMoreElements()) {
            Element element = (Element) e.nextElement();
            String type           = element.getAttribute("type");
            String id             = element.getAttribute("id");
            if (id.equals("")) {
                id = type;
            }
            contentTypes.put(id, type);
            String defaultEscaper = element.getAttribute("defaultescaper");
            if (! defaultEscaper.equals("")) {
                if (charTransformers.containsKey(defaultEscaper)) {
                    defaultEscapers.put(id, defaultEscaper);
                } else {
                    log.warn("Default escaper '" + defaultEscaper + "' for type + '"+ type + "' is not known");
                }
            }
            String defaultPostprocessor = element.getAttribute("defaultpostprocessor");
            if (! defaultPostprocessor.equals("")) {
                if (charTransformers.containsKey(defaultPostprocessor)) {
                    defaultPostProcessors.put(id, defaultPostprocessor);
                } else {
                    log.warn("Default postprocessor '" + defaultPostprocessor + "' for type + '"+ type + "' is not known");
                }
            }
            String defaultEncoding = element.getAttribute("defaultencoding");
            if (! defaultEncoding.equals("NOTSPECIFIED")) {
                defaultEncodings.put(id, defaultEncoding);
            }
        }


    }
    

    private Attribute type           = Attribute.NULL;
    private Attribute encoding       = Attribute.NULL;
    private Attribute escaper        = Attribute.NULL;
    private Attribute postprocessor  = Attribute.NULL;
    private Attribute expires        = Attribute.NULL;
    private Attribute clear          = Attribute.NULL;


    public void setType(String ct) throws JspTagException {
        type = getAttribute(ct);
    }

    public void setEncoding(String e) throws JspTagException {
        encoding = getAttribute(e);
    }

    public void setEscaper(String e) throws JspTagException {
        escaper = getAttribute(e);
    }

    public void setPostprocessor(String e) throws JspTagException {
        postprocessor = getAttribute(e);
    }

    public void setExpires(String e) throws JspTagException {
        expires = getAttribute(e);
    }


    public String getType() throws JspTagException {
        if (type == Attribute.NULL) {
            return "text/html"; // implicit
        } else {
            String ct = type.getString(this);
            String c = (String) contentTypes.get(ct);
            if (c != null) return c;
            return ct;
        }
    }



    /*
    protected int getVersion() {
        return 1;
    }
    */
    

    /**
     * @return A CharTransformer or null if no postprocessing needed
     * @throws JspTagException can occur if taglibcontent.xml is misconfigured
     */
    protected CharTransformer getPostProcessor() throws JspTagException {
        if (! postprocessor.getString(this).equals("")) {
            return getCharTransformer(postprocessor.getString(this), getContextProvider().getContextContainer(), this);
        } else {
            if (type != Attribute.NULL) {
                String defaultPostProcessor = (String) defaultPostProcessors.get(type.getString(this));
                if (defaultPostProcessor != null) {
                    return getCharTransformer(defaultPostProcessor,  getContextProvider().getContextContainer(), this);
                }
            }
            return null;
        }
    }


    public String getEncoding() throws JspTagException {
        if (encoding == Attribute.NULL) {
            String defaultEncoding = (String) defaultEncodings.get(getType());
            if (defaultEncoding == null) {
                return "UTF-8"; // implicit
            } else {
                return defaultEncoding;
            }
        } else {
            return encoding.getString(this);
        }
    }

    /**
     * Gets a CharTransformer identified by <code>id<code>, withouth trying to create chains of
     * them.
     */
    protected static CharTransformer getSimpleCharTransformer(String id, Map more, ContextReferrerTag tag) throws JspTagException {
        CharTransformer c = (CharTransformer) charTransformers.get(id);
        if (c == null && more != null) c = (CharTransformer) more.get(id);
        if (c == null) {
            int paramsPos = id.indexOf('(');
            if (paramsPos > 0 && id.charAt(id.length() - 1) == ')') { // inline parameterized
                                                                           // like substring(2,3)
                String parameterized = id.substring(0, paramsPos);
                ParameterizedTransformerFactory factory = getTransformerFactory(parameterized);                
                Parameters parameters = factory.createParameters();
                parameters.setAutoCasting(true);
                if (tag != null) {
                    tag.fillStandardParameters(parameters);
                }
                parameters.setAll(StringSplitter.split(id.substring(paramsPos + 1, id.length() - 1)));
                c = (CharTransformer) factory.createTransformer(parameters);
            } else {
                // try if there is a factory with this name, which would work with only 'standard' parameters.
                ParameterizedTransformerFactory factory = getTransformerFactory(id); 
                log.info("Found factory for " + id + " " + factory);
                if (factory != null) {
                    Parameters parameters = factory.createParameters();
                    parameters.setAutoCasting(true);
                    if (tag != null) {
                        tag.fillStandardParameters(parameters);
                    }
                    c = (CharTransformer) factory.createTransformer(parameters);                
                }
            }
        }
        if (c == null) throw new JspTagException("The chartransformer " + id + " is unknown");
        return c;
    }

    /**
     * Gets a CharTransformer identified by <code>id</code>, which possibly can also be list of id's
     * in which case a chain of chartransformers will be returned.
     * @return A CharTransformer
     * @throws JspTagException if not transformer with given id was configured
     */

    public static CharTransformer getCharTransformer(String id, Map more, ContextReferrerTag tag) throws JspTagException {

        List transs = org.mmbase.util.StringSplitter.splitFunctions(id);
        
        if (transs.size() > 1) {
            ChainedCharTransformer ct = new ChainedCharTransformer();
            // Iterator ids = StringSplitter.split(id).iterator();
            Iterator ids = transs.iterator();
            while (ids.hasNext()) {
                String i = (String) ids.next();
                CharTransformer c = getSimpleCharTransformer(i, more, tag);
                if (ct != COPY) {
                    ct.add(c);
                }
            }
            return ct;
        } else {
            CharTransformer ct =  getSimpleCharTransformer(id, more, tag);
            if (ct != COPY) {
                return ct;
            } else {
                return null;
            }

        }
    }

    public static ParameterizedTransformerFactory getTransformerFactory(String id) throws JspTagException {
        ParameterizedTransformerFactory fact = (ParameterizedTransformerFactory) parameterizedCharTransformerFactories.get(id);
            if (fact == null) throw new JspTagException("The chartransformerfactory " + id + " is unknown");
            return fact;
    }
    
    /** 
     * Called by children
     * @return A CharTransformer (not null)
     */

    public CharTransformer getWriteEscaper() throws JspTagException {
        return (CharTransformer) pageContext.getAttribute(ESCAPER_KEY);
    }
    private CharTransformer prevEscaper = null;

    protected void setWriteEscaper() throws JspTagException {
        prevEscaper = getWriteEscaper();
        CharTransformer esc;
        if (! escaper.getString(this).equals("")) { 
            esc =  getCharTransformer(escaper.getString(this), getContextProvider().getContextContainer(), this);
        }  else {
            String defaultEscaper = (String) defaultEscapers.get(getType());
            if (defaultEscaper != null) {                
                esc = getCharTransformer(defaultEscaper, getContextProvider().getContextContainer(), this);
            } else {
                esc = COPY;
            }
        }
        pageContext.setAttribute(ESCAPER_KEY, esc);
    }
    protected void unsetWriteEscaper() {
        if (prevEscaper == null) {
            pageContext.removeAttribute(ESCAPER_KEY);
        } else {
            pageContext.setAttribute(ESCAPER_KEY, prevEscaper);
        }
    }



    public int doStartTag() throws JspTagException {
        super.doStartTag();
        setWriteEscaper();
        String type = getType();

        if (! type.equals("")) {
            HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            response.setLocale(locale);
            String enc  = getEncoding();
            log.debug("Found encoding " + enc);
            if (enc.equals("")) {
                response.setContentType(getType()); // sadly, tomcat does not allow for not setting the charset, it will simply do it always
            } else {
                response.setContentType(getType() + ";charset=" + enc);
            }

            if (expires == Attribute.NULL && request.getSession(false) == null) { // if no session, can as well cache in proxy
                long later = System.currentTimeMillis() + DEFAULT_EXPIRE_TIME * 1000;
                response.setDateHeader("Expires", later);
                response.setHeader("Cache-Control", "public");
            } else {
                // there is a session, or 'expires' was set explicitely
                
                // perhaps default cache behaviour should be no-cache if there is a session?
                long exp = expires.getLong(this, DEFAULT_EXPIRE_TIME);
                if (exp <= 0) { // means : cannot be cached!                    
                    response.setHeader("Pragma", "no-cache"); // not really defined what should do this on response. Cache-Control should actually do the work.                    
                    response.setHeader("Cache-Control", "no-store");
                    // according to rfc2616 sec 14 also 'no-cache' should have worked, but apache 2 seems to ignore it.
                    
                    // long now = System.currentTimeMillis();                        
                    // according to  rfc2616 sec14 'already expires' means that date-header is expires header
                    // sadly, this does not work:
                    // perhaps because tomcat overrides the date header later, so a difference of a second can occur
                    // response.setDateHeader("Date",     now);                         
                    // response.setDateHeader("Expires",  now); 
                    
                } else {
                    long later = System.currentTimeMillis() + exp * 1000;
                    response.setDateHeader("Expires", later);
                    response.setHeader("Cache-Control", "public");
                }                    
            }
        }
        if (getPostProcessor() == null) {
            log.debug("no postprocessor");
            return EVAL_BODY; 
        } else {
            return EVAL_BODY_BUFFERED;
        }
    }

    public int doEndTag() throws JspTagException {
        unsetWriteEscaper();
        return super.doEndTag();
    }


    /** 
     * Sets a user. This is used by cloud-tag. It does not do it if the user is anonymous, so for
     * the moment it is only checked for 'null'.
     */

    void setUser(UserContext newUser) throws JspTagException {
        //user = newUser;
        if (newUser != null) {
            long exp = expires.getLong(this, DEFAULT_EXPIRE_TIME);
            if (exp > 0) { 
                HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
                // This page is using the non-anonymous cloud. Cache control must be private.
                response.setHeader("Cache-Control", "private");
            }
        }
    }

    public int doAfterBody() throws JspTagException { 
        if (bodyContent != null) {
            CharTransformer post = getPostProcessor();
            if (post != null) {
                if (log.isDebugEnabled()) {
                    log.debug("A postprocessor was defined " + post);
                    // log.trace("processing  " + bodyContent.getString());
                }
                
                post.transform(bodyContent.getReader(), bodyContent.getEnclosingWriter());
                
            } else {
                if (EVAL_BODY == EVAL_BODY_BUFFERED) {
                    // only needed for lousy app-servers
                    try {
                        if (bodyContent != null) {
                            bodyContent.writeOut(bodyContent.getEnclosingWriter());
                        }
                    } catch (java.io.IOException ioe){
                        throw new TaglibException(ioe);
                    }                     
                }
            }
        }
        return SKIP_BODY;
    }



}

