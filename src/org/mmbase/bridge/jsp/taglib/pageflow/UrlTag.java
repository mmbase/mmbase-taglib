/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.pageflow;

import java.util.*;

import org.mmbase.util.*;
import org.mmbase.framework.*;
import org.mmbase.bridge.jsp.taglib.*;
import org.mmbase.bridge.jsp.taglib.util.Attribute;


import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpServletRequest;


import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;


/**
 * A Tag to produce an URL with parameters. It can use 'context' parameters easily.
 *
 * @author Michiel Meeuwissen
 * @version $Id: UrlTag.java,v 1.116 2008-04-11 14:06:53 michiel Exp $
 */

public class UrlTag extends CloudReferrerTag  implements  ParamHandler, FrameworkParamHandler {

    private static final Logger log           = Logging.getLoggerInstance(UrlTag.class);

    private static Boolean makeRelative       = null;
    protected Attribute  referids             = Attribute.NULL;
    protected Map<String, Object> extraParameters      = null;
    protected Map<String, Object> frameworkParameters  = null;
    protected UrlParameters parameters;
    protected Attribute  page                 = Attribute.NULL;
    protected Attribute  block                = Attribute.NULL;
    protected Attribute  escapeAmps           = Attribute.NULL;
    protected Attribute  absolute             = Attribute.NULL;
    private   Attribute  encode               = Attribute.NULL;
    protected Url        url;

    public void setReferids(String r) throws JspTagException {
        referids = getAttribute(r);
    }

    public void setPage(String p) throws JspTagException {
        page = getAttribute(p);
    }

    public void setBlock(String b) throws JspTagException {
        block = getAttribute(b);
    }

    public void setEscapeamps(String e) throws JspTagException {
        escapeAmps = getAttribute(e);
    }

    public void setEncode(String e) throws JspTagException {
       encode = getAttribute(e);
    }
    /**
     * @since MMBase-1.8
     */
    public void setAbsolute(String a) throws JspTagException {
        absolute = getAttribute(a);
    }

    /**
     * @since MMBase-1.9
     */
    protected String getAbsolute() throws JspTagException {
        return absolute.getString(this);
    }

    /**
     * @since MMBase-1.9
     */
    protected boolean encode() throws JspTagException {
        return encode.getBoolean(this, true);
    }
    /**
     * @since MMBase-1.9
     */

    protected boolean escapeAmps() throws JspTagException {
        return escapeAmps.getBoolean(this, true);
    }


    public void addParameter(String key, Object value) throws JspTagException {
        Url.addParameter(extraParameters, key, value);
        if (url != null) {
            url.invalidate();
        }
        if (log.isDebugEnabled()) {
            log.debug("adding parameter " + key + "/" + value + "--> "  + parameters);
        }
    }

    /**
     * @since MMBase-1.9
     */
    public void addFrameworkParameter(String key, Object value) throws JspTagException {
        Url.addParameter(frameworkParameters, key, value);
        if (url != null) {
            url.invalidate();
        }
        if (log.isDebugEnabled()) {
            log.debug("adding parameter " + key + "/" + value + "--> "  + frameworkParameters);
        }
    }

    protected void initTag(boolean internal) throws JspTagException {
        extraParameters = new HashMap<String, Object>();
        frameworkParameters = new HashMap<String, Object>();
        parameters = new UrlParameters(this);
        helper.useEscaper(false);
        if (referid != Attribute.NULL) {
            if (page != Attribute.NULL) {
                throw new TaglibException("Cannot specify both 'referid' and 'page' attributes");
            }

            Object o = getObject(getReferid());
            if (o instanceof Url) {
                Url u = (Url) getObject(getReferid());
                extraParameters.putAll(u.params);
                frameworkParameters.putAll(u.frameworkParams);
                url = new Url(this, u, frameworkParameters, parameters, internal);
            } else {
                url = new Url(this,
                              getPage(Casting.toString(o)),
                              frameworkParameters,
                              parameters, internal);
            }
        } else {
            url = new Url(this, getPage(getPage()), frameworkParameters, parameters, internal);
        }

        if (getId() != null) {
            //parameters.getWrapped(); // dereference this Why? That would break mm:param's.
            getContextProvider().getContextContainer().register(getId(), url);
        }

    }


    public int doStartTag() throws JspTagException {
        helper.initTag();
        initTag(false);
        return EVAL_BODY_BUFFERED;
    }

    protected String getPage(String p) throws JspTagException {
        return p;
    }

    /**
     * Return the page.
     */
    protected String getPage() throws JspTagException {
        return page.getString(this);
    }

    /**
     * If it would be nice that an URL starting with '/' would be generated relatively to the current request URL, then this method can do it.
     * If the URL is not used to write to (this) page, then you probably don't want that.
     *
     * The behaviour can be overruled by starting the URL with two '/'s.
     *
     * @since MMBase-1.7
     */
    protected StringBuilder makeRelative(StringBuilder show) {
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        if (show.charAt(0) == '/') { // absolute on servletcontex
            if (show.length() > 1 && show.charAt(1) == '/') {
                log.debug("'absolute' url, not making relative");
                show.deleteCharAt(0);
                show.insert(0, req.getContextPath());
            } else {
                log.debug("'absolute' url");
                String thisDir = new java.io.File(req.getServletPath()).getParent();
                show.insert(0,  org.mmbase.util.UriParser.makeRelative(thisDir, "/")); // makes a relative path to root.
            }
        }
        return show;
    }

    /**
     * Whether URL must be generatored relatively. This default to false, and can be configured with
     * the servlet context init parameter 'mmbase.taglib.url.makerelative'. It can be useful to be
     * sure that url's are relative, if e.g. the context path is taken away in an URL-rewrite (e.g. by proxy).
     * This might give problems with redirects, but if you happen to solve that too, or don't do that...
     *
     * @since MMBase-1.7
     */
    protected boolean doMakeRelative() {
        if (makeRelative == null) {
            String setting = pageContext.getServletContext().getInitParameter("mmbase.taglib.url.makerelative");
            makeRelative = "true".equals(setting) ? Boolean.TRUE : Boolean.FALSE;
        }
        return makeRelative.booleanValue();
    }


    /**
     * Returns the component assiociated with this url. This is either the 'current' component, the
     * or <code>null</code>
     * @since MMBase-1.9
     */
    protected Component getComponent() throws JspTagException {
        return Url.getComponent(this);
    }

    /**
     * The specified parameters, by the referids attirbute and by sub-param-tags.
     * @since MMBase-1.9
     */
    protected Map<String, Object> getParameters() {
        return parameters;
    }

    protected void doAfterBodySetValue() throws JspTagException {
        if (url != null) {
            helper.setValue(url);
        }
    }

    public int doAfterBody() throws JspException {
        if (bodyContent != null) bodyContent.clearBody(); // don't show the body.
        return helper.doAfterBody();
    }


    protected void initDoEndTag() throws JspTagException {
        if (helper.getJspvar() == null) {
            helper.overrideWrite(true);
            // because Url tag can have subtags (param), default writing even with body seems sensible
            // unless jspvar is specified, because then, perhaps the user wants that..
        }

    }
    public int doEndTag() throws JspTagException {
        if (log.isDebugEnabled()) {
            log.debug("endtag of url tag " + parameters + " -> " + url.get());
        }
        if (getId() != null) {
            parameters.getWrapped(true); // dereference, and calculate
        }
        initDoEndTag();
        doAfterBodySetValue();
        helper.doEndTag();
        extraParameters = null;
        frameworkParameters = null;
        parameters = null;
        return super.doEndTag();
    }


}
