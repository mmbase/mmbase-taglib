/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import java.io.IOException;
import java.io.File;

import java.util.*;

import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.RequestDispatcher;

import org.mmbase.bridge.*;
import org.mmbase.bridge.ContextProvider;
import org.mmbase.security.Rank; // hmm, not from bridge, but we do need it anyway

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * Creates a cloud object (pulling it from if session necessary). While
 * creating a cloud object one also has to authenticate itself, so this
 * functionality is also in this tag.
 *
 * @author Pierre van Rooden
 * @author Michiel Meeuwissen
 * @author Vincent van der Locht
 * @version $Id: CloudTag.java,v 1.107 2005-02-07 09:23:15 andre Exp $
 */

public class CloudTag extends ContextReferrerTag implements CloudProvider {

    private static final int METHOD_UNSET = -1;
    private static final int METHOD_HTTP = 0;
    private static final int METHOD_ASIS = 1;
    private static final int METHOD_ANONYMOUS = 2;
    private static final int METHOD_LOGOUT = 3;
    private static final int METHOD_LOGINPAGE = 4;
    // private static final int METHOD_GIVEN_OR_ANONYMOUS = 5;
    private static final int METHOD_DELEGATE = 6;
    private static final int METHOD_SESSIONDELEGATE = 7;
    private static final int METHOD_PAGELOGON = 8;
    private static final int METHOD_SESSIONLOGON = 9;

    /**
     * Constants needed for the loginpage attribute functionality
     */
    private static final String LOGINPAGE_COMMAND_PARAMETER = "command";
    private static final String LOGINPAGE_COMMAND_LOGIN = "login";
    private static final String LOGINPAGE_COMMAND_LOGOUT = "logout";
    private static final String LOGINPAGE_AUTHENTICATE_PARAMETER = "authenticate";
    private static final String LOGINPAGE_CLOUD_PARAMETER = "cloud";

    private static final String LOGINPAGE_DENYREASON_NEED = "please";
    private static final String LOGINPAGE_DENYREASON_FAIL = "failed";
    private static final String LOGINPAGE_DENYREASON_RANKTOOLOW = "rank";

    private static final int DENYREASON_NEED = 0;
    private static final int DENYREASON_FAIL = 1;
    private static final int DENYREASON_RANKTOOLOW = 2;

    private static final Logger log = Logging.getLoggerInstance(CloudTag.class);

    private static final String DEFAULT_CLOUD_NAME = "mmbase";
    private static final String DEFAULT_AUTHENTICATION = "name/password";

    private static final String REALM = "realm_";

    private String jspVar;

    private Cookie[] cookies;

    private CloudContext cloudContext;

    private Attribute cloudName = Attribute.NULL;
    private Attribute cloudURI = Attribute.NULL;
    private Cloud cloud;

    /**
     * @since MMBases-1.7
     */
    private boolean sessionCloud = true;

    private Attribute authenticate = Attribute.NULL;

    private Attribute loginpage = Attribute.NULL;

    //private int method = METHOD_UNSET; // how to log on, method can eg be 'http'.
    private Attribute method = Attribute.NULL;
    private Attribute logonatt = Attribute.NULL;
    private List logon;
    private Attribute pwd = Attribute.NULL;
    private Attribute rank = Attribute.NULL;
    private Attribute sessionName = Attribute.NULL;

    private Attribute onfail = Attribute.NULL;

    private static final String FAILMESSAGE = "<h1>CloudTag Error</h1>";

    private HttpSession session;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Locale locale;

    /**
     * @return the default cloud context
     **/
    public CloudContext getDefaultCloudContext() throws JspTagException {
        if (cloudContext == null) {
            cloudContext = ContextProvider.getCloudContext(cloudURI.getString(this));
        }
        return cloudContext;
    }

    public void setUri(String uri) throws JspTagException {
        cloudURI = getAttribute(uri);
    }

    public void setName(String name) throws JspTagException {
        cloudName = getAttribute(name);
    }

    protected String getName() throws JspTagException {
        if (cloudName == Attribute.NULL) {
            return DEFAULT_CLOUD_NAME;
        }
        return cloudName.getString(this);
    }

    public void setLogon(String l) throws JspTagException {
        logonatt = getAttribute(l);
    }
    public void setPwd(String pwd) throws JspTagException {
        this.pwd = getAttribute(pwd);
    }

    /**
     * Synonym for setLogon. Don't mix.
     */
    public void setUsername(String l) throws JspTagException {
        logonatt = getAttribute(l);
    }

    /**
     * Synonym for setPwd. Don't mix.
     */
    public void setPassword(String pwd) throws JspTagException {
        this.pwd = getAttribute(pwd);
    }

    public void setRank(String r) throws JspTagException {
        rank = getAttribute(r);
    }

    /**
     * Gives the configured rank as a Rank object
     */

    protected Rank getRank() throws JspTagException {
        String s = rank.getString(this);
        Rank r = Rank.getRank(s);
        if (r == null) {
            throw new JspTagException("Unknown rank " + s);
        }
        return r;
    }

    /**
     * @since MMBase-1.7
     */
    public void setOnfail(String of) throws JspTagException {
        onfail = getAttribute(of);
    }

    /**
     * If this cloud is 'anonymous' according to rank attribute.
     * @since MMBase-1.7
     */
    private boolean rankAnonymous() throws JspTagException {
        if (rank == Attribute.NULL)
            return true;
        String rankString = rank.getString(this);
        return rankString.equals("") || rankString.equals(Rank.ANONYMOUS.toString());
    }

    public void setJspvar(String jv) {
        jspVar = jv;
    }

    public void setAuthenticate(String authenticate) throws JspTagException {
        if (!"".equals(authenticate)) { // this makes it easier to ignore.
            this.authenticate = getAttribute(authenticate);
        } else {
            this.authenticate = Attribute.NULL;
        }
    }

    protected String getAuthenticate() throws JspTagException {
        String a = authenticate.getString(this);
        if (a.equals("")) {
            return DEFAULT_AUTHENTICATION;
        }
        return a;
    }

    public void setMethod(String mm) throws JspTagException {
        method = getAttribute(mm);
    }

    protected int getMethod() throws JspTagException {
        String m = method.getString(this).toLowerCase();
        if ("".equals(m)) {
            return METHOD_UNSET;
        } else if ("http".equals(m)) {
            return METHOD_HTTP;
        } else if ("asis".equals(m)) {
            return METHOD_ASIS;
        } else if ("anonymous".equals(m)) {
            return METHOD_ANONYMOUS;
        } else if ("logout".equals(m)) {
            return METHOD_LOGOUT;
        } else if ("loginpage".equals(m)) {
            return METHOD_LOGINPAGE;
        } else if ("delegate".equals(m)) {
            return METHOD_DELEGATE;
        } else if ("sessiondelegate".equals(m)) {
            return METHOD_SESSIONDELEGATE;
        } else if ("pagelogon".equals(m)) {
            return METHOD_PAGELOGON;
        } else if ("sessionlogon".equals(m)) {
            return METHOD_SESSIONLOGON;
            //} else if ("given_or_anonymous".equals(m)) {
            //    return METHOD_GIVEN_OR_ANONYMOUS;
        } else {
            throw new JspTagException("Unknown value for 'method'  attribute (" + m + ")");
        }
    }

    public Cloud getCloudVar() {
        return cloud;
    }

    public void setCloudVar(Cloud c) {
        cloud = c;
    }

    public void setSessionname(String s) throws JspTagException {
        if (!s.equals("")) {
            sessionName = getAttribute(s);
        }
    }

    public void setLoginpage(String loginpage) throws JspTagException {
        this.loginpage = getAttribute(loginpage);
    }

    /**
     * @return The cookie with the sessionname.
     */

    private Cookie searchCookie() throws JspTagException {
        String cookie = REALM + getSessionName();
        if (log.isDebugEnabled()) {
            log.debug("Searching cookie " + cookie);
        }
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(cookie) && (!"".equals(cookies[i].getValue()))) {
                    return cookies[i];
                }
            }
        }
        log.debug("Cookie not found");
        return null;
    }

    /**
     * Sets the 'realm' to r, and write it to the session, or if not possible in a cookie.
     * Changing the realm makes it possible to 'logout' when using http-authentication.
     * @return false on failure
     */
    private boolean setRealm(String r) throws JspTagException {
        if (session == null) {
            log.debug("setting realm in cookie"); // Is this ever used? Can the session be null in Tomcat or so?
            Cookie c = searchCookie();
            if (c == null) {
                c = new Cookie(REALM + getSessionName(), r);
                String path = request.getContextPath();
                if (path.equals("")) path = "/";
                c.setPath(path);
                c.setMaxAge(-1); // duration of browser
            } else {
                c.setValue(r);
            }
            if (cookies.length == 0) {
                cookies = new Cookie[1];
            }
            cookies[0] = c;
            response.addCookie(c);
        } else {
            if (session.isNew()) {
                log.debug("New session!? That is very suspicious. Perhaps URL was not encoded, and cookies disabled, sending redirect to make sure the url is encoded.");
                String query = request.getQueryString();
                String thisPage = request.getRequestURI() + (query == null ? "" : "?" + query);
                try {
                    String url = response.encodeRedirectURL(thisPage);
                    response.sendRedirect(url);
                } catch (IOException e) {
                    throw new TaglibException(e);
                }
                return false;
            }
            log.debug("setting realm in session");
            session.setAttribute(REALM + getSessionName(), r);
        }
        return true;
    }
    /**
     * Removing the realm will cause a new one to be created, and if
     * you have a good browser, it will pop up a new 'login' screen (when using http-authentication).
     */

    private void removeRealm() throws JspTagException {
        if (session == null) {
            String cookie = REALM + getSessionName();
            log.debug("removing cookie");
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals(cookie)) {
                        if (log.isDebugEnabled()) {
                            log.debug("removing cookie with value " + cookies[i]);
                        }
                        cookies[i].setValue("");
                        cookies[i].setMaxAge(0); // remove
                        response.addCookie(cookies[i]);
                    }
                }
            }
        } else {
            session.removeAttribute(REALM + getSessionName());
        }
    }
    /**
     * Gets the current realm in use for http-authentication.
     */
    private String getRealm() throws JspTagException {
        if (session == null) { // try with cookie
            Cookie c = searchCookie();
            if (c != null) {
                if (log.isDebugEnabled()) {
                    log.debug("found cookie on path = " + c.getPath());
                }
                return c.getValue();
            }
            return null;
        } else {
            return (String) session.getAttribute(REALM + getSessionName());
        }
    }

    /**
     * Deny access to this page.
     *
     * @param A message to provide to the user if she does not accepts authorization (enters 'cancel')
     *
     * REMARK: Perhaps we should do some i18n on this message
     *
     * @return SKIP_BDDY;
     */
    private int denyHTTP(String message) throws JspTagException {
        log.debug("sending deny");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        if (getRealm() == null) {
            String contextPath = request.getContextPath();
            if (!setRealm("MMBase" + contextPath + "@" + request.getServerName())) {
                return SKIP_BODY;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("setting header: " + getRealm());
        }
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + getRealm() + "\"");

        //res.setHeader("Authorization", logon);   would ne nice...
        //keesj:look at the php3 tutorial for an example

        // if cancel
        // this cannot be done with an exception (which can be a redirect), because the page must
        // flow ahead, to give the popup opportunity to pop up.
        try {
            pageContext.getOut().print(FAILMESSAGE + message);
        } catch (IOException ioe) {
            throw new TaglibException(ioe);
        }
        setAnonymousCloud(); // there must also be _some_ cloud, to avoid exception on
        evalBody(); //  register var
        return SKIP_BODY;
    }

    private boolean setAnonymousCloud() throws JspTagException {
        return setAnonymousCloud(null);
    }

    /**
     * Sets the cloud member variable to an anonymous cloud.
     * @return true on success (cloud is set), false on failure (cloud is set to null)
     */

    private boolean setAnonymousCloud(Map logoutInfo) throws JspTagException {
        try {
            // request/response information is not needed for mmbase-only implementation.
            // but when using 'delegated' login-method, you might also want to delegate logout.
            if (log.isDebugEnabled()) {
                log.debug("creating an anonymous cloud for cloud '" + getName() + "' (with " + logoutInfo + ")");
            }
            // removeCloud(); // should not remove existing cloud from session
            cloud = getDefaultCloudContext().getCloud(getName(), "anonymous", logoutInfo);
            cloud.setLocale(locale);
            return true;
        } catch (java.lang.SecurityException e) {
            // login failed for anymous ?! That's odd, provide null.
            log.info("Could not create anonymous cloud because " + e.toString());
            cloud = null;
            return false;
        } catch (Throwable t) {
            throw new TaglibException("Could not create anonymous cloud because " + t.getClass().getName() + ": " + t.getMessage(), t);
        }
    }

    /**
     * Return the name of session variable in which the cloud is
     * stored. This is on default "cloud_mmbase" but this can be
     * influenced with the sessionname attribute. If two pages have
     * different sessionnames for the cloud, they can be logged in
     * simultaniously with different clouds, but in the same session.
     * If no sessionname is given or an empty sessionname, it returns the defaultvalue.
     */
    public String getSessionName() throws JspTagException {
        String sn = sessionName.getString(this);
        if (sn.equals("")) {
            return "cloud_" + getName();
        } else {
            return sn;
        }
    }

    /**
     * If the 'cloud' member is set, and everything is ok, then this function is called in doStartTag()
     * @return EVAL_BODY or SKIP_BODY if cloud is null (which normally means that no anonymous is present)
     */

    private int evalBody() throws JspTagException {

        if (getId() != null) { // write to context.
            getContextProvider().getContextContainer().register(getId(), cloud);
        }

        if (cloud == null) {
            return SKIP_BODY;
        }

        if (jspVar != null) {
            log.debug("Setting jspVar " + jspVar);
            Object was = pageContext.getAttribute(jspVar);
            if (was != null && ! was.equals(cloud)) {
                throw new JspTagException("Jsp-var '" + jspVar + "' already in pagecontext! (" + was + "), can't write " + cloud + " in it. This may be a backwards-compatibility issue. This may be a backwards-compatibility issue. Change jspvar name or switch on backwards-compatibility mode (in your web.xml)");
            }
            pageContext.setAttribute(jspVar, cloud);
        }

        if (locale != null) {
            cloud.setLocale(locale);
        }

        // the surround context tag sometimes also want so server information from the cloud context.
        getContextTag().setCloudContext(cloud.getCloudContext());

        ContentTag tag = (ContentTag) findParentTag(ContentTag.class, null, false);
        if (tag != null) {
            User user = cloud.getUser();
            if (sessionCloud && ! user.getRank().equals(org.mmbase.security.Rank.ANONYMOUS.toString())) {
                tag.setUser(cloud.getUser());
            }
        }

        return EVAL_BODY;
    }

    /**
     * Checks if this is a 'reuse' of the cloud tag (using the referid
     * attribute), and sets the cloud member variable if this is the
     * cases
     *
     * @return true if this is a reuse (the caller returns eval-body) and false otherwise (the caller continues).
     */

    private final boolean checkReuse() throws JspTagException {
        if (getReferid() != null) {
            if (method != Attribute.NULL || logonatt != Attribute.NULL) { // probably add some more
                throw new JspTagException("The 'referid' attribute of cloud cannot be used together with 'method' or 'logon' attributes");
            }
            log.debug("found cloud with referid");
            cloud = (Cloud) getContextProvider().getContextContainer().getObject(getReferid());
            return true;
        }
        return false;
    }

    /**
     * Checks if an anonymous cloud is requested. If so, set the cloud variable accordingly.
     * @return true if cloud must be anonymous (the caller returns eval-body) and false otherwise (the caller continues).
     */

    private final boolean checkAnonymous() throws JspTagException {
        setAnonymousCloud();
        if ((method == Attribute.NULL && logon == null && rankAnonymous() && loginpage == Attribute.NULL) || getMethod() == METHOD_ANONYMOUS) { // anonymous cloud:
            log.debug("Implicitely requested anonymous cloud. Not using session");

            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the cloud of the session is requested to be 'logged out'.
     */
    private final boolean checkLogoutLoginPage() throws JspTagException {
        if (loginpage != Attribute.NULL && LOGINPAGE_COMMAND_LOGOUT.equals(request.getParameter(LOGINPAGE_COMMAND_PARAMETER))) {
            log.debug("request to log out, remove session atributes, give anonymous cloud.");
            if (cloud != null) {
                removeRealm();
                if (session != null) {
                    log.debug("ok. session is not null");
                    session.removeAttribute(getSessionName()); // remove cloud itself
                }
            }
            setAnonymousCloud();
            return true;
        }
        return false;
    }

    /**
     * Checks if the cloud of the session if requested to be 'logged out'.
     */
    private final boolean checkLogoutMethod() throws JspTagException {
        if (getMethod() == METHOD_LOGOUT) {
            if (cloud != null) {
                removeRealm();
                if (session != null) {
                    log.debug("ok. session is not null");
                    session.removeAttribute(getSessionName()); // remove cloud itself
                }
            }
            Map logoutInfo = new HashMap();
            logoutInfo.put("request", request);
            logoutInfo.put("response", response);
            setAnonymousCloud(logoutInfo);
            return true;
        }
        return false;
    }

    /**
     * Creates the member variables session and cookies variables member
     */
    private final void setupSession() throws JspTagException {
        cookies = request.getCookies();
        if (cookies == null) {
            cookies = new Cookie[0];
        }
        if (log.isDebugEnabled()) {
            log.debug("getting (thus creating) session now: " + session);
        }
        session = pageContext.getSession();
        if (session != null) { // some people like to disable their session
            Object c = session.getAttribute(getSessionName());
            if (c != null && ! (c instanceof Cloud)) {
                throw new TaglibException("The session variable '" + getSessionName() + "' is not of type Cloud (but it is a '" + c.getClass().getName() + "'), and perhaps is used for another goal. This error could be avoided by use of the 'sessionname' attribute of the cloud-tag.");
            }
            cloud = (Cloud) c;
            if (cloud != null) {
                if (cloud.getUser().isValid()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Created/found a session. Cloud in it is of: " + cloud.getUser());
                    }
                    
                } else {
                    if (log.isDebugEnabled()) {                        
                        log.debug("Found invalid cloud in session of '" + cloud.getUser() + "'. Discarding.");
                    }                    
                    cloud = null;
                }
            } else {
                log.debug("No cloud found");
            }
            
        } else {
            log.debug("Not succeeded creating a session");
        }

    }

    /**
     * Checks if there is a surround locale tag, and sets the 'locale' member variable accordingly.
     */

    private final void checkLocale() throws JspTagException {
        locale = null;
        LocaleTag localeTag = (LocaleTag) findParentTag(LocaleTag.class, null, false);
        if (localeTag != null) {
            locale = localeTag.getLocale();
        }

    }

    /**
     * Checks wether the cloud is requested 'as is', meaning that is must be tried to get it from the session.
     *
     */

    private final boolean checkAsis() throws JspTagException {
        if (getMethod() == METHOD_ASIS) {
            session = request.getSession(false);
            if (session != null) {
                cloud = (Cloud) session.getAttribute(getSessionName());
            }
            if (cloud == null) {
                setAnonymousCloud();
            }
            if (cloud != null) {
                checkValid();
            }
            checkCloud();
            return true;
        }
        return false;

    }

    /**

     */

    private final void checkValid() {
        if (!cloud.getUser().isValid()) {
            log.debug("found a cloud in the session, but is was expired, throwing it away");
            cloud = null;
        }
    }

    /**
     * @since MMBase-1.7
     */
    private final void removeCloud() throws JspTagException {
        cloud = null;
        if (session != null) {
            session.removeAttribute(getSessionName());
        }
    }

    /**
     * Checks if the current cloud satisfies the other conditions set
     * on it (method=anonymous, logon, rank attribute). If not the
     * cloud member variable is made null.
     */
    private final void checkCloud() throws JspTagException {
        // we have a cloud, check if it is a desired one
        // otherwise make it null.
        if (log.isDebugEnabled()) {
            log.debug("found cloud in session m: " + method + " l: " + logon + ". Checking it");
        }

        if (cloud == null || cloud.getUser() == null || (!cloud.getUser().isValid())) {
            // Makes the cloud variable null (may not be null already) if it
            // is 'expired'. This means normally that the security
            // configuration has been changed, or MMBase restarted or
            // something like that. This means that the cloud (probably gotten
            // from the session), cannot be used anymore.
            log.debug("found a cloud in the session, but is was expired, throwing it away");
            removeCloud();
            return;
        }

        int meth = getMethod();
        if (logon == null && rank == Attribute.NULL && meth != METHOD_UNSET && meth != METHOD_ASIS) {
            // authorisation was requested, but not indicated for whom
            if (log.isDebugEnabled()) {
                log.debug("Implicitily requested non-anonymous (by method) cloud. Current user: " + cloud.getUser().getIdentifier());
            }
            if (cloud.getUser().getRank().equals(Rank.ANONYMOUS.toString())) { // so it simply may not be anonymous
                log.debug("there was a cloud, but anonymous. log it on");
                removeCloud();
                return;
            }

        } else if (logon != null && cloud != null) {
            if (log.isDebugEnabled()) {
                log.debug("Explicitily requested non-anonymous cloud (by logon = '" + logon + "'). Current user: " + cloud.getUser().getIdentifier());
            }
            // a logon name was given, check if logged on as the right one
            if (!logon.contains(cloud.getUser().getIdentifier())) { // no!
                log.debug("logged on, but as wrong user. log out first.");
                removeCloud();
                return;
            } else {
                log.debug("Cloud is ok already");
            }
        }
        if (!rankAnonymous()) {
            if (log.isDebugEnabled()) {
                log.debug("Explicitily requested non-anonymous cloud (by rank). Current user: " + cloud.getUser());
            }
            Rank curRank = Rank.getRank(cloud.getUser().getRank());
            if (curRank.getInt() < getRank().getInt()) {
                if (log.isDebugEnabled()) {
                    log.debug("logged on, but rank of user (" + curRank.toString() + ") is too low (must be " + getRank().toString() + "). log out first.");
                }
                removeCloud();
                return;
            } else {
                log.debug("Cloud is ok already");
            }

        }
        if (meth != METHOD_UNSET && meth != METHOD_ASIS && cloud != null && (!cloud.getUser().getAuthenticationType().equals(getAuthenticate()))) {
            log.debug(
                "Cloud was logged on with different authentication type ('"
                    + cloud.getUser().getAuthenticationType()
                    + "' in stead of the requested '" + getAuthenticate()
                    + "'. Should do procedure again.");
            removeCloud();
            return;
        } else {
            log.debug("Cloud was logged with same authentication type -> ok");
        }

    }

    /**
     * Sets logon and password using http-authentication in the Map argument.
     * @param uses A Map which will be passed to the security system.
     * @return SKIP_BODY if a deny was sent, the caller must not
     * continue then. EVAL_BODY  otherwise (can be ignored).
     */

    private final int doHTTPAuthentication(Map user) throws JspTagException {
        log.debug("with http");

        if (getRealm() == null) {
            log.debug("no realm found, need to log on again");
            return denyHTTP("<h2>Need to log in again</h2> You logged out");
        }
        if (log.isDebugEnabled()) {
            log.debug("authent: " + request.getHeader("WWW-Authenticate") + " realm: " + getRealm());
        }
        // find logon, password with http authentication
        String userName = null;
        String password = null;
        try {
            String mime_line = request.getHeader("Authorization");
            if (mime_line != null) {
                String user_password = org.mmbase.util.Encode.decode("BASE64", mime_line.substring(6));
                StringTokenizer t = new StringTokenizer(user_password, ":");
                if (t.countTokens() == 2) {
                    userName = t.nextToken();
                    password = t.nextToken();
                }
            }
        } catch (Exception e) {
            log.error("oooops " + e);
        }
        // Authenticate user
        if (log.isDebugEnabled()) {
            log.debug("u " + userName + " p " + password);
        }

        if (logon != null) { // if there was a username specified as well, it must be the same
            log.debug("http with username");
            if (!logon.contains(userName)) {
                log.debug("username not correct");
                return denyHTTP("<h2>Wrong username</h2> must be " + logon + "");
            } else {
                logon = new ArrayList();
                logon.add(userName);
            }
        } else { // logon == null
            log.debug("http without username");
            if (userName == null) { // there must be at least known a username...
                log.debug("no username known");
                return denyHTTP("<h2>No username given</h2>");
            }
            /*
              logon = new ArrayList();
              logon.add(userName); why is this..
            */
        }
        user.put("username", userName);
        user.put("password", password);
        return EVAL_BODY;

    }

    /**
     * Sets logon-variables using the login page (with the loginpage attribute) in the Map argument.
     * @param uses A Map which will be passed to the security system,.
     * @return SKIP_BODY if a redirect was given (to the login page),
     * the caller must not continue then. EVAL_BODY  otherwise
     * (can be ignored).
     */

    private int doLoginPage(Map user) throws JspTagException {
        log.debug("login page required to acces this cloud data!");

        // look if we need to login(param command='login') with methods and with the params...
        // otherwise redirect!
        if (LOGINPAGE_COMMAND_LOGIN.equals(request.getParameter(LOGINPAGE_COMMAND_PARAMETER))) {

            if (log.isDebugEnabled()) {
                log.debug("Request to login with loginpage, trying to perform an login");
            }
            // now pass all the attributes to the cloud-login method of the cloudcontext!
            String authenticatePassed = request.getParameter(LOGINPAGE_AUTHENTICATE_PARAMETER);
            if (authenticatePassed != null) {
                setAuthenticate(authenticatePassed); // THIS SEEMS DANGEROUS
            }
            String cloudNamePassed = request.getParameter(LOGINPAGE_CLOUD_PARAMETER);
            if (cloudNamePassed != null) {
                setName(cloudNamePassed); // THIS SEEM DANGEROUS
            }
            Enumeration enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()) {
                String key = (String) enumeration.nextElement();
                String value = request.getParameter(key);
                if (log.isDebugEnabled()) {
                    log.debug("security info --> key:" + key + " value:" + value);
                }
                user.put(key, value);
            }
            return EVAL_BODY;
        } else {
            // no command give, send redirect to specified login page
            return denyLoginPage(LOGINPAGE_DENYREASON_NEED, "");
        }
    }

    /**
     * Denies access to this page and sends are redirect to the login-page (given by login page attribute).
     * @param reason       The reason for deny, can be LOGINPAGE_DENYREASON_NEED or LOGINPAGE_DENYREASON_FAIL.
     * @param excactReason A  further specification of the reason (any String)
     * @return SKIP_BODY
     */
    private int denyLoginPage(String reason, String exactReason) throws JspTagException {
        try {

            // find this page relative to login-page
            String referrerPage = null;

            String requestURI = request.getRequestURI();
            if (requestURI.endsWith("/")) {
                referrerPage = ".";
            } else {
                referrerPage = new File(requestURI).getName();
            }


            /*
            if (1 == 0) { 
                // XXXXX hmm, should test this in freeze
                // making relative urls'.

                String toDir = new File(toFile).getParent();
                if (toDir == null) {
                    toDir = ".";
                }
                File servletPath = new File(request.getServletPath());

                String thisDir = servletPath.getParent();
                if (thisDir == null) {
                    thisDir = ".";
                }

                String thisFile = servletPath.getName();

                if (toDir.startsWith("/")) {
                    referrerPage = org.mmbase.util.UriParser.makeRelative(toDir, thisDir) + "/" + thisFile;
                } else {
                    referrerPage = org.mmbase.util.UriParser.makeRelative(thisDir + "/" + toDir, toDir) + "/" + thisFile;
                }
            }
            */



            String toFile = loginpage.getString(this);

            String referrer = null;

            // if a 'referrer' is explicitely mentioned in the 'loginpage' attribute (e.g. on a 'dologin' page), we try to honour it.
            int existingQueryPosition = toFile.indexOf('?');
            if (existingQueryPosition > 0) {
                String existingQuery = toFile.substring(existingQueryPosition + 1);
                log.debug("Found existing query " + existingQuery);
                String[] parameters = existingQuery.split("&");
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].startsWith("referrer=")) {
                        referrer = org.mmbase.util.Encode.decode("escape_url", parameters[i].substring(9));
                        if (referrer.startsWith("?")) referrer = "." + referrer; // tomcat 5, referrerPage can be "", which is inconvenient, because using it as action for login.jsp whill be empty string, will post to login.jsp again
                        log.debug("Found existing referrer " + referrer);
                        break;
                    }
                }
            }
            if (referrer == null) {
                if (request.getQueryString() != null) {
                    referrer = referrerPage + "?" + request.getQueryString();
                } else {
                    referrer = referrerPage;
                }
            }
            //reference = org.mmbase.util.Encode.encode("ESCAPE_URL_PARAM", reference);
            RequestDispatcher rd = request.getRequestDispatcher(toFile);
            request.setAttribute("referrerpage", referrerPage);
            request.setAttribute("referrer", referrer);
            request.setAttribute("reason", reason);
            request.setAttribute("exactreason", exactReason);
            rd.forward(request, response);
            return SKIP_BODY;
        } catch (javax.servlet.ServletException ioe) {
            throw new TaglibException("error sending redirect", ioe);
        } catch (java.io.IOException ioe) {
            throw new TaglibException("error sending redirect", ioe);
        }
    }

    private final int deny(int reason, String exactReason) throws JspTagException {

        int method = getMethod();
        // did not succeed, so problably the password was wrong.
        if (method == METHOD_HTTP) { // give a deny, people can retry the password then.
            switch (reason) {
            case DENYREASON_RANKTOOLOW :
                return denyHTTP("<h2>Rank too low for this page (must be at least " + getRank().toString() + ")</h2>");
            case DENYREASON_FAIL :
            default :
                return denyHTTP("<h2>This page requires authentication</h2>");
            }
        } else if (method == METHOD_LOGINPAGE || (method == METHOD_UNSET && loginpage != Attribute.NULL)) {
            switch (reason) {
            case DENYREASON_RANKTOOLOW :
                return denyLoginPage(LOGINPAGE_DENYREASON_RANKTOOLOW, exactReason);
            case DENYREASON_FAIL :
            default :
                return denyLoginPage(LOGINPAGE_DENYREASON_FAIL, exactReason);
            }
        } else if (method == METHOD_DELEGATE || method == METHOD_SESSIONDELEGATE) {
            switch (reason) {
            case DENYREASON_RANKTOOLOW :
                throw new JspTagException("Rank too low");
            case DENYREASON_FAIL :
            default :
                return SKIP_BODY;
            }
        } else { // strange, no method given, password wrong (or missing), that's really wrong.
            switch (reason) {
            case DENYREASON_FAIL : {
                if ("name/password".equals(getAuthenticate())) {
                    throw new JspTagException("Logon of with "
                                              + (logon != null && logon.size() > 0 ? "'" + logon.get(0) + "'" : "''")
                                              + " failed."
                                              + (pwd == Attribute.NULL ? " (no password given)" : " (wrong password)"));
                } else {
                    throw new JspTagException("Authentication ('" + getAuthenticate() + "') failed");
                }
            }
            case DENYREASON_RANKTOOLOW : {
                throw new JspTagException("Rank too low");
            }
            default :
                throw new JspTagException("Denied for unknown reason");
            }
        }
    }

    /**
     * Performs logging in the bridge and sets the cloud variable, using the provided Map argument.
     *
     * @return SKIP_BODY on fail and EVAL_BODY otherwise.
     *
     */
    private final int doLogin(Map user) throws JspTagException {
        log.debug("Username found. logging in");
        try {
            cloud = getDefaultCloudContext().getCloud(getName(), getAuthenticate(), user);
            log.debug("Logged in");
            // ok, logging on work, now check rank if necessary
            if (rank != Attribute.NULL) {
                log.debug("Checking for rank");
                Rank curRank = Rank.getRank(cloud.getUser().getRank());
                Rank r = getRank();
                if (curRank.getInt() < r.getInt()) {
                    if (log.isDebugEnabled()) {
                        log.debug("logged on, but rank of user is too low (" + cloud.getUser().getRank() + ". log out first.");
                    }
                    cloud = null;
                    if (session != null) {
                        session.removeAttribute(getSessionName());
                    }
                    log.debug("rank to low");
                    return deny(DENYREASON_RANKTOOLOW, "" + curRank + " < " + r);
                }
            }
            return EVAL_BODY;
        } catch (java.lang.SecurityException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to log in with " + user + " because " + e.toString());
            }
            return deny(DENYREASON_FAIL, e.getMessage());
        }
    }

    /**
     * Makes a logged-in cloud.
     * @return SKIP_BODY if failed to do so. EVAL_BODY  otherwise.
     */

    private final int makeCloud() throws JspTagException {
        log.debug("logging on the cloud...");
        Map user = null;
        int method = getMethod();

        // check how to log on:
        if (method == METHOD_DELEGATE || method == METHOD_SESSIONDELEGATE) {
            user = new HashMap();
            user.put("request", request);
            user.put("response", response);
            user.put("usernames", logon);
            sessionCloud = method == METHOD_SESSIONDELEGATE;
        } else if (method == METHOD_HTTP) {
            user = new HashMap();
            sessionCloud = true;
            if (doHTTPAuthentication(user) == SKIP_BODY) {
                return SKIP_BODY;
            }
        } else if (loginpage != Attribute.NULL && (method == METHOD_LOGINPAGE || method == METHOD_UNSET)) {
            user = new HashMap();
            sessionCloud = true;
            if (doLoginPage(user) == SKIP_BODY) {
                return SKIP_BODY;
            }
        } else if (logon != null && pwd != Attribute.NULL) {
            user = new HashMap();
            user.put("username", logon.get(0));
            user.put("password", pwd.getString(this));
            if (method == METHOD_PAGELOGON) {
                sessionCloud = false;
            } else {
                if (method != METHOD_SESSIONLOGON) {
                    log.warn("Using logon/pwd (or username/password) attributes on page '" + request.getRequestURI() + "' without specifying method='[pagelogon|sessionlogon]', defaulting to 'sessionlogon'. Be aware that users of this page now are authenticated in their session!");
                }
                sessionCloud = true;
            }
        } else {
            sessionCloud = false;
        }

        // do the MMCI cloud logging on
        if (user != null) {
            if (doLogin(user) == SKIP_BODY) {
                return SKIP_BODY;
            }
        } else {
            log.debug("no login given, creating anonymous cloud");
            // no logon, create an anonymous cloud.
            setAnonymousCloud();
        }
        checkCloud(); // perhaps the just created cloud does not satifisfy other conditions? (rank)
        if (cloud == null) { // stil null, give it up then...
            /*
            user = new HashMap();
            if (doHTTPAuthentication(user) == SKIP_BODY) {
                return SKIP_BODY;
            }
            */

            log.debug("Could not create Cloud.");
            // throw new JspTagException("Could not create cloud (even not anonymous)");
            return SKIP_BODY;
        } else {
            if (session != null && sessionCloud) {
                session.setAttribute(getSessionName(), cloud);
            }
        }
        return EVAL_BODY;
    }

    /**
     * request and response can be determined once a page..
     */

    public void setPageContext(PageContext pc) {
        super.setPageContext(pc);
        request = (HttpServletRequest) pageContext.getRequest();
        response = (HttpServletResponse) pageContext.getResponse();
        if (log.isDebugEnabled()) {
            log.debug("Got a " + response.getClass().getName());
        }
    }

    /**
     *  Sets the cloud variable considering all requirements. SKIP_BODY if this can not be done.
     *
     */
    public int doStartTag() throws JspTagException {
        checkLocale();

        {
            String s = logonatt.getString(this);
            // logon = s.equals("") ? null : StringSplitter.split(s);
            logon = s.equals("") ? null : Arrays.asList( s.trim().split("\\s*,\\s*") );
        }

        if (checkReuse()) { // referid
            return evalBody();
        }
        if (checkAnonymous()) { // check if requested, and create
            if (cloud == null) { // could not be created!
                // what can we do now?                
                return SKIP_BODY;
            } else {
                // yes, found
                log.debug("Implicitely requested anonymous cloud. Will not use session");
                return evalBody();
            }
        }
        if (checkAsis()) { // checks if request 'asis'
            if (cloud == null) { // could not even make anonymous
                return SKIP_BODY;
            } else {
                return evalBody();
            }
        }
        setupSession(); // might need session now
        if (log.isDebugEnabled()) {
            log.debug("startTag " + cloud);
        }
        if (checkLogoutLoginPage()) {
            // TODO: find a better page to redirect to!
            try {
                String url = request.getRequestURI();
                response.sendRedirect(url);
            } catch (java.io.IOException ioe) {
                throw new TaglibException(ioe);
            }
            return SKIP_BODY;
        }
        if (checkLogoutMethod()) { 
            return evalBody();
        }

        if (cloud != null) {
            checkCloud();
        }
        if (cloud == null) {
            if (makeCloud() == SKIP_BODY) { // we did't have a cloud, or it was not a good one:
                return SKIP_BODY;
            }
        }
        //pwd = Attribute.NULL; // could not be right. Using cloud-tag twice in same page might not work then

        return evalBody();
    }

    public int doEndTag() throws JspTagException {
        // can be cleaned for gc:
        cookies = null;
        cloudContext = null;
        cloud = null;
        logon = null;
        session = null;
        request = null;
        response = null;
        return super.doEndTag();
    }

    // if EVAL_BODY == EVAL_BODY_BUFFERED
    public int doAfterBody() throws JspTagException {

        if (EVAL_BODY == EVAL_BODY_BUFFERED) {
            try {
                if (bodyContent != null) {
                    bodyContent.writeOut(bodyContent.getEnclosingWriter());
                }
            } catch (IOException ioe) {
                throw new TaglibException(ioe);
            }
        }
        return SKIP_BODY;
    }

}
