/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.jsp.taglib;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.jsp.taglib.containers.*;
import org.mmbase.bridge.Query;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspException;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;


/**
 * The index of current item of a list.
 *
 * @author Michiel Meeuwissen
 * @version $Id: IndexTag.java,v 1.17 2004-04-22 17:27:09 michiel Exp $ 
 */

public class IndexTag extends ListReferrerTag implements Writer, QueryContainerReferrer {

    private static final Logger log = Logging.getLoggerInstance(IndexTag.class);


    private Attribute container = Attribute.NULL;

    /**
     * @since MMBase-1.7.1
     */
    public void setContainer(String c) throws JspTagException {
        container = getAttribute(c);
    }


    private Attribute offset = Attribute.NULL; 

    public void setOffset(String o) throws JspTagException {
        offset = getAttribute(o);
    }
    protected int getOffset() throws JspTagException {
        return offset.getInt(this, getList().getIndexOffset()); // start counting at list's offset on default (normally 1)
    }

    public int doStartTag() throws JspTagException{

        int index;
        if (container != Attribute.NULL) {
            if (parentListId != Attribute.NULL) {
                throw new JspTagException("Cannot specify both 'container' and 'list' attributes");
            }
            QueryContainer c = (QueryContainer) findParentTag(QueryContainer.class, (String) container.getValue(this));
            Query query = c.getQuery();
            index = query.getOffset() / query.getMaxNumber() + offset.getInt(this, 0);
        } else if (parentListId != Attribute.NULL) {
            index = getList().getIndex()  + getOffset();;
        } else {
            QueryContainerOrListProvider tag = (QueryContainerOrListProvider) findParentTag(QueryContainerOrListProvider.class, null);
            if (tag instanceof QueryContainer) {
                Query query = ((QueryContainer) tag).getQuery();
                index = query.getOffset() / query.getMaxNumber() + offset.getInt(this, 0);
            } else {
                index = ((ListProvider) tag).getIndex() + getOffset();
            }
        }

        
        helper.setValue(new Integer(index));
        if (getId() != null) {
            getContextProvider().getContextContainer().register(getId(), helper.getValue());
        }
        return EVAL_BODY_BUFFERED;
    }


    public int doAfterBody() throws JspException {
        return helper.doAfterBody();
    }

    /**
     *
     **/
    public int doEndTag() throws JspTagException {
        return helper.doEndTag();
    }

}
