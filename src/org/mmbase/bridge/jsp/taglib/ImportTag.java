/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;
import org.mmbase.bridge.jsp.taglib.util.*;
import javax.servlet.jsp.JspTagException;
import javax.servlet.http.*;

import org.mmbase.util.transformers.CharTransformer;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

import java.util.*;

/**
 * The importtag puts things in the context. It can find them from the
 * environment or from its body.
 *
 * @author Michiel Meeuwissen
 * @see    ContextTag
 * @version $Id: ImportTag.java,v 1.48 2005-01-06 20:23:39 michiel Exp $
 */

public class ImportTag extends ContextReferrerTag {
    private static final Logger log = Logging.getLoggerInstance(ImportTag.class);

    protected Attribute required = Attribute.NULL;
    protected Attribute from     = Attribute.NULL;

    protected Attribute externid = Attribute.NULL;
    private   Attribute reset    = Attribute.NULL;

    private   boolean found = false;
    private   String  useId = null;

    /**
     * The extern id it the identifier in some external source.
     */

    public void setExternid(String e) throws JspTagException {
        externid = getAttribute(e);
    }

    /**
     * If 'required' then the variable must be available in the
     * external source, otherwise exception.
     *
     */
    public void setRequired(String b) throws JspTagException {
        required = getAttribute(b);
    }


    /**
     * If 'required' then the variable must be available in the
     * external source, otherwise exception.
     *
     */
    public void setReset(String b) throws JspTagException {
        reset = getAttribute(b);
    }

    /**
     * From which external source
     */

    public void setFrom(String s) throws JspTagException {
        from = getAttribute(s);
    }


    public int doStartTag() throws JspTagException {
        Object value = null;
        helper.overrideWrite(false);
        log.trace("dostarttag of import");
        
        if (getId() == null) {
            log.trace("No id was given, using externid ");
            useId = (String) externid.getValue(this);
        } else {
            useId = getId();
            if (log.isDebugEnabled()) log.trace("An id was given (" + id + ")");
        }

        
        if (externid != Attribute.NULL) {

            boolean res = reset.getBoolean(this, false);
            if (log.isDebugEnabled()) { 
                log.trace("Externid was given " + externid.getString(this));
            }
            if (from.getString(this).equals("")) {
                found = (getContextProvider().getContextContainer().findAndRegister(pageContext, externid.getString(this), useId, ! res) != null);
            } else {
                List fromsList = from.getList(this);
                boolean searchThis =  fromsList.contains("this") || fromsList.contains("THIS");

                if (searchThis) {
                    res = true;
                }
                Iterator froms = fromsList.iterator();
                ContextContainer cc = getContextProvider().getContextContainer();
                while (froms.hasNext() && ! found) {
                    int from = ContextContainer.stringToLocation((String) froms.next());
                    Object result = cc.find(pageContext, from, externid.getString(this));
                    if (from == ContextContainer.LOCATION_THIS && result == null) {
                        result = cc.find(pageContext, from, useId);
                    }
                    if (result != null) {
                        cc.register(useId, result, ! res);
                        found = true;
                        break;
                    }
                }
            }

            if (! found && required.getBoolean(this, false)) {
                String fromString = from.getString(this).toLowerCase();
                if (fromString.equals("session") && ((HttpServletRequest) pageContext.getRequest()).getSession(false) == null) {
                    throw new JspTagException("Required parameter '" + externid.getString(this) + "' not found in session, because there is no session");
                }
                throw new JspTagException("Required parameter '" + externid.getString(this) + "' not found " + (fromString.equals("") ? "anywhere" : ("in " + fromString)));
            }
            if (found) {
                value = getObject(useId);
                if (log.isDebugEnabled()) {
                    log.debug("found value for " + useId + " '" + value + "'");
                }
            }
        }
        if (found) {
            setValue(value, WriterHelper.NOIMPLICITLIST);
            if (useId != null) {
                getContextProvider().getContextContainer().reregister(useId, helper.getValue());
            }
            return SKIP_BODY;
        } else {
            setValue(null);
            return EVAL_BODY_BUFFERED;
        }

    }

    /**
     * Retrieves the value from the writer-helper, but escapes if necessary (using 'escape' attribute)
     * @since MMBase-1.7.2
     */
    protected void setValue(Object value, boolean noImplicitList) throws JspTagException {
        if (helper.getEscape() != null) {
            CharTransformer escaper  = ContentTag.getCharTransformer(helper.getEscape());
            if (escaper != null && value != null) {
                value = escaper.transform((String) value);
            }
        }
        helper.setValue(value, noImplicitList);
    }
    /**
     * @since MMBase-1.7.2
     */
    protected void setValue(Object value) throws JspTagException {
        setValue(value, WriterHelper.IMPLICITLIST);
    }


    public int doEndTag() throws JspTagException {
        if (log.isDebugEnabled()) {
            log.debug("endtag of import with id:" + id + " externid: " + externid.getString(this));
        }
        if (externid != Attribute.NULL) {
            if (! found ) {
                if (log.isDebugEnabled()) log.debug("External Id " + externid.getString(this) + " not found");
                // try to find a default value in the body.
                Object body = bodyContent != null ? bodyContent.getString() : "";
                if (! "".equals(body)) { // hey, there is a body content!
                    if (log.isDebugEnabled()) {
                        log.debug("Found a default in the body (" + body + ")");
                    }
                    setValue(body);
                    getContextProvider().getContextContainer().reregister(useId, helper.getValue());
                }  else {
                    //  might be vartype="list" or so, still need to set
                    setValue(null);
                    getContextProvider().getContextContainer().reregister(useId, helper.getValue());
                }
            }
        } else { // get value from the body of the tag.
            setValue(bodyContent != null ? bodyContent.getString() : "");
            if (useId != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting " + useId + " to " + helper.getValue());
                }
                boolean res = reset.getBoolean(this, false); // should this be more general? Also in other contextwriters?
                getContextProvider().getContextContainer().register(useId, helper.getValue(), !res);
            } else {
                if (helper.getJspvar() == null) {
                    found = false; // for use next time
                    useId = null;
                    throw new JspTagException("Attributes externid, id and jspvar cannot be all missing");
                }
            }
        }
        found = false; // for use next time
        useId = null;
        bodyContent = null;
        helper.doEndTag();
        log.debug("end of importag");
        super.doEndTag();
        return EVAL_PAGE;
    }


}
