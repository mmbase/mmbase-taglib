/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.pageflow;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.jsp.taglib.util.Referids;
import org.mmbase.bridge.jsp.taglib.TaglibException;
import org.mmbase.bridge.jsp.taglib.ContextTag;
import org.mmbase.bridge.NotFoundException;
import java.net.*;
//import javax.net.ssl.*;
import java.io.*;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.http.*;
import javax.servlet.*;

import java.util.*;

import org.mmbase.util.*;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * Like UrlTag, but does not spit out an URL, but the page itself.
 *
 * @author Michiel Meeuwissen
 * @author Johannes Verelst
 * @version $Id: IncludeTag.java,v 1.58 2005-03-16 23:23:52 michiel Exp $
 */

public class IncludeTag extends UrlTag {

    private static  final Logger log = Logging.getLoggerInstance(IncludeTag.class);
    private static  final Logger pageLog = Logging.getLoggerInstance(Logging.PAGE_CATEGORY);

    private static final int DEBUG_NONE = 0;
    private static final int DEBUG_HTML = 1;
    private static final int DEBUG_CSS  = 2;

    public static final String INCLUDE_PATH_KEY   = "javax.servlet.include.servlet_path";
    public static final String INCLUDE_LEVEL_KEY = "org.mmbase.taglib.includeLevel";


    protected Attribute debugType = Attribute.NULL;

    private Attribute cite = Attribute.NULL;

    private Attribute encodingAttribute = Attribute.NULL;

    private   Attribute  attributes       = Attribute.NULL;

    /**
     * Test whether or not the 'cite' parameter is set
     */
    public void setCite(String c) throws JspTagException {
        cite = getAttribute(c);
    }

    public void setEncoding(String e) throws JspTagException {
        encodingAttribute = getAttribute(e);
    }

    protected boolean getCite() throws JspTagException {
        return cite.getBoolean(this, false);
    }

    public void setAttributes(String a) throws JspTagException {
        attributes = getAttribute(a);
    }

    public int doStartTag() throws JspTagException {
        if (page == null) { // for include tags, page attribute is obligatory.
            throw new JspTagException("Attribute 'page' was not specified");
        }
        return super.doStartTag();
    }

    protected void doAfterBodySetValue() throws JspTagException {
        includePage();
    }

    /**
     * Opens an Http Connection, retrieves the page, and returns the result.
     **/
    private void external(BodyContent bodyContent, String absoluteUrl, HttpServletRequest request, HttpServletResponse response) throws JspTagException {
        if (log.isDebugEnabled()) {
            log.debug("External: found url: >" + absoluteUrl + "<");
        }
        try {
            URL includeURL = new URL(absoluteUrl);

            HttpURLConnection connection = (HttpURLConnection) includeURL.openConnection();

            /*
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }); 
            }
            */

            int responseCode = connection.getResponseCode();
            if (responseCode >= 300) {
                if (responseCode >= 500) {
                    log.warn("Server error " + responseCode + " during mm:include of " + includeURL + " " + connection.getResponseMessage());
                } else if (responseCode >= 400) {
                    log.warn("Client error " + responseCode + " during mm:include of " + includeURL + " " + connection.getResponseMessage());
                } 
                log.warn("Redirect " + responseCode + " during mm:include of " + includeURL + " " + connection.getResponseMessage() + " " + connection.getInstanceFollowRedirects());
            }
            if (request != null) {
                // Also propagate the cookies (like the jsession...)
                // Then these, and the session,  also can be used in the include-d page
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    StringBuffer koekjes = new StringBuffer();
                    for (int i=0; i < cookies.length; i++) {
                        if (log.isDebugEnabled()) {
                            log.debug("setting cookie " + i + ":" + cookies[i].getName() + "=" + cookies[i].getValue());
                        }
                        koekjes.append((i > 0 ? ";" : "")).append(cookies[i].getName()).append("=").append(cookies[i].getValue());
                    }
                    connection.setRequestProperty("Cookie", koekjes.toString());
                }
            }

            String encoding = encodingAttribute.getString(this);
            if (encoding.equals("")) {            
                encoding = connection.getContentEncoding();
            }
            log.debug("Found content encoding " + encoding);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();
            int c = inputStream.read();
            while (c != -1) {
                bytes.write(c);
                c = inputStream.read();
            }
            byte[] allBytes = bytes.toByteArray();
            if (encoding == null || encoding.equals("")) {
                String contentType = connection.getContentType();
                if (contentType != null) {
                    // according to http://www.w3.org/TR/2002/NOTE-xhtml-media-types-20020801/, 'higher level' charset indication should prevail
                    encoding = GenericResponseWrapper.getEncoding(contentType);
                }

                if (encoding == null && contentType != null) { // take a default based on the content type
                    encoding = GenericResponseWrapper.getDefaultEncoding(contentType);
                }
                if (encoding.equals(GenericResponseWrapper.TEXT_XML_DEFAULT_CHARSET)) { // if content-type is text/xml the body should be US-ASCII, which we will ignore and evalute the body. See comments in GenericResponseWrapper#getDefaultEncoding. 
                    encoding = GenericResponseWrapper.getXMLEncoding(allBytes); // Will detect if body is XML, and set encoding to something if it is, otherwise, remains null.
                }

            }
            log.debug("Using " + encoding);
            helper.setValue(debugStart(absoluteUrl) + new String(allBytes, encoding) + debugEnd(absoluteUrl));

            if (log.isDebugEnabled()) {
                log.debug("found string: " + helper.getValue());
            }

        } catch (IOException e) {
            throw new TaglibException (e);
        }
    }

    /**
     * Include a local file by doing a new HTTP request
     * Do not use this method, but use the 'internal()' method instead
     */
    private void externalRelative(BodyContent bodyContent, String relativeUrl, HttpServletRequest request, HttpServletResponse response) throws JspTagException {
        external(bodyContent,
                 request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + relativeUrl,
                 request, response);
    }

    /**
     * Use the RequestDispatcher to include a page without doing a request.
     * Encoding apparently work, but why they do isn't very clear.
     */
    private void internal(BodyContent bodyContent, String relativeUrl, HttpServletRequest req, HttpServletResponse resp) throws JspTagException {
        if (log.isDebugEnabled()) {
            log.debug("Internal: found url: >" + relativeUrl + "<");
            String targetEncoding   = resp.getCharacterEncoding();
            log.debug("encoding: " + targetEncoding);
            log.debug("req Parameters");
            Map params = req.getParameterMap();
            Iterator i = params.entrySet().iterator();
            Object o;
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                o = e.getValue();
                if (o instanceof String[]) {
                    log.debug("key '" + e.getKey() + "' value '" + Arrays.asList((String[]) o) + "'");
                } else {
                    log.debug("key '" + e.getKey() + "' value '" + o.toString() + "'");
                }                
            }
        } 

        req.removeAttribute(ContextTag.CONTEXTTAG_KEY);

        if (attributes != Attribute.NULL) {
            Iterator i = Referids.getReferids(attributes, this).entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry) i.next();
                req.setAttribute((String) entry.getKey(), entry.getValue());
            }
            
        }
        // Orion bug fix.
        req.getParameterMap();

        HttpServletRequestWrapper requestWrapper   = new HttpServletRequestWrapper(req);

        try {
            ServletContext sc = pageContext.getServletContext();
            if (sc == null) log.error("Cannot retrieve ServletContext from PageContext");
            RequestDispatcher requestDispatcher = sc.getRequestDispatcher(relativeUrl);
            if (requestDispatcher == null) {
                throw new NotFoundException("Page \"" + relativeUrl + "\" does not exist (No request-dispatcher could be created)");
            }

            GenericResponseWrapper responseWrapper;
            String encoding = encodingAttribute.getString(this);
            if (encoding.equals("")) { 
                responseWrapper = new GenericResponseWrapper(resp);
            } else {
                responseWrapper = new GenericResponseWrapper(resp, encoding);
            }

            requestDispatcher.include(requestWrapper, responseWrapper);                
            String page = responseWrapper.toString();
            helper.setValue(debugStart(relativeUrl) + page + debugEnd(relativeUrl));                


        } catch (Throwable e) {
            log.error(relativeUrl + " " +  Logging.stackTrace(e));
            throw new TaglibException(relativeUrl + " " + e.getMessage(), e);
        }

        if (log.isDebugEnabled()) {
            log.debug("req Parameters");
            Map params = req.getParameterMap();
            Iterator i = params.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                log.debug("key '" + e.getKey() + "' value '" + e.getValue() + "'");
            }
        }
    }



    /**
     * When staying in the same web-application, then the file also can be found on the file system,
     * and the possibility arises simply citing it (passing the web-server). It is in no way
     * interpreted then. This can be useful when creating example pages.
     * @param bodyContent unused
     * @param relativeUrl URL to cite relative to root of web-app.
     * @param request     unused
     */
    private void cite(BodyContent bodyContent, String relativeUrl, HttpServletRequest request) throws JspTagException {
        try {
            if (log.isDebugEnabled()) log.trace("Citing " + relativeUrl);
            if (relativeUrl.indexOf("..") > -1 || relativeUrl.toUpperCase().indexOf("WEB-INF") > -1)  { // toUpperCase: just for windows, of course
                throw new JspTagException("Not allowed to cite " + relativeUrl);
            }

            // take of the sessionid if it is present
            //HttpSession session = request.getSession(false);
            //if (session != null && session.isNew())
            { // means there is a ;jsession argument
                int j = relativeUrl.lastIndexOf(';');
                if (j != -1) {
                    relativeUrl = relativeUrl.substring(0, j);
                }
                
            }


            String resource = relativeUrl;
            if (log.isDebugEnabled()) log.debug("Citing " + resource);
            
            
            Reader reader = ResourceLoader.getWebRoot().getReader(resource);
            StringWriter writer = new StringWriter();
            if (reader == null) {
                writer.write("No such resource " + resource);
            } else {
                while (true) {
                    int c = reader.read();
                    if (c == -1) break;
                    writer.write(c);
                }
            }
            helper.setValue(debugStart(resource) + writer.toString() + debugEnd(resource));
        } catch (IOException e) {
            throw new TaglibException (e);
        }
    }

    /**
     * Includes another page in the current page.
     */
    protected void includePage() throws JspTagException {
        try {


            String gotUrl = getUrl(false, false); // false, false: don't write &amp; tags but real & and don't urlEncode

            if (pageLog.isServiceEnabled()) {
                pageLog.service("Parsing mm:include JSP page: " + gotUrl);
            }

            // Do some things to make the URL absolute.
            String nudeUrl; // url withouth the params
            String params;  // only the params.
            int paramsIndex = gotUrl.indexOf('?');
            if (paramsIndex != -1) {
                nudeUrl = gotUrl.substring(0, paramsIndex);
                params  = gotUrl.substring(paramsIndex);
            } else {
                nudeUrl = gotUrl;
                params  = "";
            }
            if (log.isDebugEnabled()) {
                log.debug("Found nude url " + nudeUrl);
            }
            HttpServletRequest request   = (HttpServletRequest) pageContext.getRequest();
            HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();


            if (nudeUrl.indexOf(':') == -1) { // relative
                // Fetch include level from Attributes, mainly for debugging
                Integer level = (Integer) request.getAttribute(INCLUDE_LEVEL_KEY);
                int includeLevel;
                if (level == null) {
                    includeLevel = 0;
                } else {
                    includeLevel = level.intValue();
                }

                // Fetch the current servlet from request attribute.
                // This is needed when we are resolving relatively.
                String includingServlet = (String) request.getAttribute(INCLUDE_PATH_KEY);

                if (includingServlet == null) {
                    includingServlet = request.getServletPath();
                }
                if (log.isDebugEnabled()) {
                    log.debug("Including from : Level=" + includeLevel + " URI=" + includingServlet);
                }

                String includedServlet;
                if (nudeUrl.charAt(0) == '/') {
                    log.debug("URL was absolute on servletcontext");
                    includedServlet = gotUrl;
                } else {
                    log.debug("URL was relative");
                    // Using url-objects only because they know how to resolve relativity
                    URL u = new URL("http", "localhost", includingServlet);
                    URL dir = new URL(u, "."); // directory
                   
                    File currentDir = new File(includingServlet + "includetagpostfix"); // to make sure that it is not a directory (tomcat 5 does not redirect then)                    
                    nudeUrl = new URL(dir, nudeUrl).getFile();
                    includedServlet = nudeUrl + params;
                }

                // Increase level and put it together with the new URI in the Attributes of the request
                includeLevel++;
                request.setAttribute(INCLUDE_LEVEL_KEY, new Integer(includeLevel));
                request.setAttribute(INCLUDE_PATH_KEY, nudeUrl);
                if (log.isDebugEnabled()) {
                    log.debug("Next Include: Level=" + includeLevel + " URI=" + includedServlet);
                }

                if (getCite()) {
                    cite(bodyContent, includedServlet, request);
                } else {
                    internal(bodyContent, includedServlet, request, response);
                }
                // Reset include level and URI to previous state
                includeLevel--;
                if (includeLevel == 0) {
                    request.removeAttribute(INCLUDE_LEVEL_KEY);
                } else {
                    request.setAttribute(INCLUDE_LEVEL_KEY, new Integer(includeLevel));
                }
                if (includingServlet == null) {
                    request.removeAttribute(INCLUDE_PATH_KEY);
                } else {                 
                    request.setAttribute(INCLUDE_PATH_KEY,  includingServlet);
                }
            } else { // really absolute
                if (getCite()) {
                    cite(bodyContent, gotUrl, request);
                } else {
                    external(bodyContent, gotUrl, null, response); // null: no need to give cookies to external url
                                                                   // also no need to encode the URL.
                }
            }

        } catch (IOException e) {
            throw new TaglibException (e);
        }
        if (pageLog.isDebugEnabled()) {
            pageLog.debug("END Parsing mm:include JSP page");
        }
    }

    /**
     * With debug attribute you can write the urls in comments to the page, just before and after
     * the included page.
     */
    public void setDebug(String p) throws JspTagException {
        debugType = getAttribute(p);
    }

    protected int getDebug() throws JspTagException {

        if (debugType == Attribute.NULL) return DEBUG_NONE;

        String dtype = debugType.getString(this).toLowerCase();
        if (dtype.equals("none")) {
            return  DEBUG_NONE; // also implement the default, then people can use a variable
                               // to select this property in their jsp pages.
        } else if (dtype.equals("html")) {
            return DEBUG_HTML;
        } else if (dtype.equals("css")) {
            return DEBUG_CSS;
        } else {
            throw new JspTagException("Unknow value for debug attribute " + dtype);
        }
    }

    /**
     * Returns a name for this tag, that must appear in the debug message (in the comments)
     */
    protected String getThisName() {
        String clazz = this.getClass().getName();
        return clazz.substring(clazz.lastIndexOf(".") + 1);
    }

    /**
     * Write the comment that is just above the include page.
     */
    private String debugStart(String url) throws JspTagException {
        switch(getDebug()) {
        case DEBUG_NONE: return "";
        case DEBUG_HTML: return "\n<!-- " + getThisName() + " page = '" + url + "' -->\n";
        case DEBUG_CSS:  return "\n/* " + getThisName() +  " page  = '" + url + "' */\n";
        default: return "";
        }
    }

    /**
     * Write the comment that is just below the include page.
     */
    private String debugEnd(String url) throws JspTagException {
        switch(getDebug()) {
        case DEBUG_NONE: return "";
        case DEBUG_HTML: return "\n<!-- END " + getThisName() + " page = '" + url + "' -->\n";
        case DEBUG_CSS:  return "\n/* END " + getThisName() + " page = '" + url + "' */\n";
        default: return "";
        }
    }
}
