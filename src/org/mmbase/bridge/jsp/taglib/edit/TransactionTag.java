/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.edit;

import javax.servlet.jsp.JspTagException;

import java.io.IOException;

import org.mmbase.bridge.Cloud;
import org.mmbase.bridge.Transaction;

import org.mmbase.bridge.jsp.taglib.CloudReferrerTag;
import org.mmbase.bridge.jsp.taglib.CloudProvider;
import org.mmbase.bridge.jsp.taglib.util.Attribute;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 *
 * Creates a new Transaction.
 *
 * @author Michiel Meeuwissen
 * @version $Id: TransactionTag.java,v 1.23 2006-08-30 18:00:15 michiel Exp $ 
 */

public class TransactionTag extends CloudReferrerTag implements CloudProvider {

    private static final Logger log = Logging.getLoggerInstance(TransactionTag.class);
    protected Transaction transaction;
    protected Attribute commit = Attribute.NULL;
    protected Attribute name   = Attribute.NULL;
    protected String jspvar = null;

    public void setCommitonclose(String c) throws JspTagException {
        commit = getAttribute(c);
    }

    /**
     * @since 1.7.1 this method shadows/implements the methods below.
     * 
     * @see org.mmbase.bridge.jsp.taglib.CloudReferrerTag#getCloudVar()
     * @see org.mmbase.bridge.jsp.taglib.CloudProvider#getCloudVar()
     */
    public Cloud getCloudVar() throws JspTagException {
        return transaction;
    }

    public void setName(String s) throws JspTagException {
        name = getAttribute(s);
    }

    public void setJspvar(String jv) {
        jspvar = jv;
    }

    protected String getName() throws JspTagException {
        return (String) name.getValue(this);
    }

    /**
     *  Creates the transaction.
     */
    public int doStartTag() throws JspTagException{
        if (log.isDebugEnabled()) {
            log.debug("value of commit: " + commit);
        }
        transaction = null;
        if (getId() != null) { // look it up from session
            log.debug("looking up transaction in context");
            try {
                Object o = getObject(getId());
                if (o instanceof Transaction) {
                    transaction = (Transaction) getObject(getId());
                } else {
                    throw new JspTagException("The object with id " + getId() + " is not a transaction, but " + (o == null ? "NULL" : "a " + o.getClass()));
                }
                if (log.isDebugEnabled()) {
                    log.debug("found " + transaction);
                }
            } catch (JspTagException e) { }
        }
        if (transaction == null) { // not found in context
            String n = getName();
            if (name == null) {
                throw new JspTagException("Did not find transaction in context, and no name for transaction supplied");
            }
            transaction = findCloudProvider().getCloudVar().getTransaction(n);
            if (getId() != null) { // put it in context
                log.debug("putting transaction in context");
                getContextProvider().getContextContainer().register(getId(), transaction);
            }
        }
        if (jspvar != null) {
            pageContext.setAttribute(jspvar, transaction);
        }
        return EVAL_BODY;
    }

    protected boolean getDefaultCommit() {
        return true;
    }

    public int doEndTag() throws JspTagException {
        if (commit.getBoolean(this, getDefaultCommit())) {
            transaction.commit();
            if (getId() != null) {
                getContextProvider().getContextContainer().unRegister(getId());
            }
        }
        transaction = null;
        return super.doEndTag();
    }
    public int doAfterBody() throws JspTagException {
        if (bodyContent != null) {
            try {
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
            } catch (IOException ioe){
                throw new JspTagException(ioe.toString());
            }
        }
        return SKIP_BODY;
    }

}

