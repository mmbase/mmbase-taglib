/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.containers;

import java.util.*;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.Query;
import org.mmbase.bridge.jsp.taglib.CloudReferrerTag;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.util.Queries;
import org.mmbase.storage.search.*;

/**
 * mm:composite makes it possible to connect constraints by 'OR'.
 *
 * @author Pierre van Rooden
 * @author Michiel Meeuwissen
 * @since  MMBase-1.7
 * @version $Id: QueryCompositeConstraintTag.java,v 1.2 2004-02-11 20:40:14 keesj Exp $
 */
public class QueryCompositeConstraintTag extends CloudReferrerTag implements QueryContainerReferrer {

    // private static final Logger log = Logging.getLoggerInstance(NodeListCompositeConstraintTag.class);

    protected Attribute container  = Attribute.NULL;

    protected Attribute operator  = Attribute.NULL;

    private List constraints;

    public void setContainer(String c) throws JspTagException {
        container = getAttribute(c);
    }

    public void setOperator(String o) throws JspTagException {
        operator = getAttribute(o);
    }

    protected int getOperator() throws JspTagException {
        String op = operator.getString(this).toUpperCase();
        if (op.equals("OR")) {
            return CompositeConstraint.LOGICAL_OR;
        } else if (op.equals("AND") || op.equals("")) {
            return CompositeConstraint.LOGICAL_AND;
        } else {
            throw new JspTagException("Unknown Field Compare Operator '" + op + "'");
        }
    }

    public void addChildConstraint(Constraint cons) throws JspTagException {
        constraints.add(cons);
    }

    private Constraint addConstraint(Query query, int op, List constraints) throws JspTagException {
        Constraint newConstraint = null;
        for (Iterator i = constraints.iterator(); i.hasNext();) {
            Constraint constraint = (Constraint) i.next();
            if (newConstraint == null) {
                newConstraint = constraint;
            } else {
                newConstraint = query.createConstraint(constraint, op, newConstraint);
            }
        }
        if (newConstraint != null) {
            // if there is a OR or an AND tag, add
            // the constraint to that tag,
            // otherwise add it direct to the query
            QueryCompositeConstraintTag cons = (QueryCompositeConstraintTag) findParentTag(QueryCompositeConstraintTag.class, (String) container.getValue(this), false);
            if (cons != null) {
                cons.addChildConstraint(newConstraint);
            } else {
                newConstraint = Queries.addConstraint(query, newConstraint);
            }
        }
        return newConstraint;
    }

    public int doStartTag() throws JspTagException {
        constraints = new ArrayList();
        return EVAL_BODY_BUFFERED;
    }

    public int doAfterBody() throws JspTagException {
        QueryContainer c = (QueryContainer) findParentTag(QueryContainer.class, (String) container.getValue(this));
        Query query = c.getQuery();

        Constraint cons = addConstraint(query, getOperator(), constraints);
        return SKIP_BODY;
    }


}
