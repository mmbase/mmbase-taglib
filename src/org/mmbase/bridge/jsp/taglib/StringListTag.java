/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import java.io.IOException;
import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.jstl.core.*;

import org.mmbase.util.Casting;
import org.mmbase.bridge.jsp.taglib.util.*;

/**
 * This class makes a tag which can list strings.
 *
 * @author Michiel Meeuwissen
 * @version $Id: StringListTag.java,v 1.27 2006-04-29 17:15:57 michiel Exp $
 * @since MMBase-1.7
 */

public class StringListTag extends NodeReferrerTag implements ListProvider, Writer { // need to extend NodeRefferer becasue of AliasListTag, no MI in java.

    protected List returnList;
    protected Iterator iterator;
    protected int      currentItemIndex= -1;

    protected Attribute  max = Attribute.NULL;
    protected Attribute  comparator = Attribute.NULL;
    protected Attribute  add= Attribute.NULL;
    protected Attribute  retain = Attribute.NULL;
    protected Attribute  remove = Attribute.NULL;

    public int size(){
        return returnList.size();
    }
    public int getIndex() {
        return currentItemIndex;
    }

    public int getIndexOffset() {
        return 1;
    }

    public boolean isChanged() {
        return true;
    }
    public Object getCurrent() {
        return getWriterValue();
    }

    public void remove() {
        iterator.remove();
    }


    public void setMax(String m) throws JspTagException {
        max = getAttribute(m);
    }

    public void setAdd(String a) throws JspTagException {
        add = getAttribute(a);
    }

    public void setRetain(String r) throws JspTagException {
        retain = getAttribute(r);
    }

    public void setRemove(String r) throws JspTagException {
        remove = getAttribute(r);
    }

    protected int getMaxNumber() throws JspTagException {
        return max.getInt(this, -1);
    }

    public void setComparator(String c) throws JspTagException {
        comparator = getAttribute(c);
    }

    /**
     * Lists do implement ContextProvider
     */
    private   ContextCollector collector;



    // ContextProvider implementation
    public ContextContainer getContextContainer() {
        return collector;
    }


    /**
     * Creates the actual list of strings.
     */
    protected List getList() throws JspTagException {
        throw new JspTagException("Should use 'referid' attribute on liststrings tag");
    }

    /**
     * For use with referid
     */
    protected void  truncateList() throws JspTagException {
        if (max != Attribute.NULL) {
            int m = getMaxNumber();
            if (m > 0 && m < returnList.size()) {
                returnList = returnList.subList(0, m);
            }
        }
    }

    /**
     *
     *
     */
    public int doStartTag() throws JspTagException{

        collector = new ContextCollector(getContextProvider());


        helper.overrideWrite(false); // default behavior is not to write to page

        if (getReferid() != null) {
            Object o =  getObject(getReferid());
            if (! (o instanceof Collection)) {
                throw new JspTagException("Context variable " + getReferid() + " is not a Collection");
            }
            if (o instanceof List) {
                returnList = (List) o;
            } else {
                returnList = new ArrayList((Collection) o);
            }
            truncateList();
            if (getReferid().equals(getId())) { // in such a case, don't whine
                getContextProvider().getContextContainer().unRegister(getId());
            }
        } else {
            returnList = getList();
        }

        if (getId() != null) {
            returnList = new ArrayList(returnList);
        }
        if (add != Attribute.NULL) {
            Object addObject = getObject(add.getString(this));
            if (addObject instanceof Collection) {
                returnList.addAll((Collection) addObject);
            } else {
                returnList.add(Casting.toString(addObject));
            }
        }
        if (retain != Attribute.NULL) {
            Object retainObject = getObject(retain.getString(this));
            if (retainObject instanceof Collection) {
                returnList.retainAll((Collection) retainObject);
            } else {
                returnList.retainAll(Collections.singletonList(Casting.toString(retainObject)));
            }
        }
        if (remove != Attribute.NULL) {
            Object removeObject = getObject(remove.getString(this));
            if (removeObject instanceof Collection) {
                returnList.removeAll((Collection) removeObject);
            } else {
                returnList.remove(Casting.toString(removeObject));
            }
        }

        currentItemIndex = - 1;  // reset index

        ListSorter.sort(returnList, (String) comparator.getValue(this), this);
        iterator = returnList.iterator();
        // if we get a result from the query
        // evaluate the body , else skip the body
        if (iterator.hasNext()) {
            setNext();
            return EVAL_BODY;
        } else {
            return SKIP_BODY;
        }
    }

    public int doAfterBody() throws JspException {
        if (getId() != null) {
            getContextProvider().getContextContainer().unRegister(getId());
        }


        helper.doAfterBody();
        collector.doAfterBody();

        if (iterator.hasNext()){
            setNext();
            return EVAL_BODY_AGAIN;
        } else {
            if (EVAL_BODY == EVAL_BODY_BUFFERED) {
                if (bodyContent != null) {
                    try {
                        bodyContent.writeOut(bodyContent.getEnclosingWriter());
                    } catch (IOException ioe){
                        throw new TaglibException(ioe);
                    }
                }
            }
            return SKIP_BODY;
        }
    }
    public int doEndTag() throws JspTagException {
        if (getId() != null) {
            getContextProvider().getContextContainer().register(getId(), returnList, false);
        }
        // dereference for gc
        returnList = null;
        iterator   = null;
        if (collector != null) {
            collector.release(pageContext, getContextProvider().getContextContainer());
            collector  = null;
        }
        return  super.doEndTag();
    }

    protected void setNext() throws JspTagException {
        currentItemIndex++;
        helper.setValue(iterator.next());
        if (getId() != null) {
            getContextProvider().getContextContainer().register(getId(), helper.getValue());
        }
    }

    public LoopTagStatus getLoopStatus() {
        return new ListProviderLoopTagStatus(this);
    }

}

