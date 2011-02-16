/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.tree;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.mmbase.bridge.jsp.taglib.ListProvider;
import org.mmbase.bridge.jsp.taglib.util.*;

/**
 * @author Michiel Meeuwissen
 * @since MMBase-1.7
 * @version $Id$
 */
abstract class AbstractTreeReferrerListTag extends TreeReferrerTag implements ListProvider, DepthProvider {

    protected int depth;
    protected int index;
    protected TreeProvider tree;

    private Object prevDepthProvider;

    @Override
    public int getIndex() {
        return index;
    }
    @Override
    public int getIndexOffset() {
        return 0;
    }
    @Override
    public boolean isChanged() {
        return true;
    }
    @Override
    public Object getCurrent() {
        return depth;
    }
    @Override
    public void remove() {
        // not supported
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public void setAdd(String c) throws JspTagException {
        throw new UnsupportedOperationException();
    }
    @Override
    public void setRetain(String c) throws JspTagException {
        throw new UnsupportedOperationException();
    }
    @Override
    public void setRemove(String c) throws JspTagException {
        throw new UnsupportedOperationException();
    }


    protected   ContextCollector  collector;

    // ContextProvider implementation

    @Override
    public ContextContainer getContextContainer() throws JspTagException {
        return collector;
    }


    protected final void doStartTagHelper() throws JspTagException {
        prevDepthProvider = pageContext.getAttribute(DepthProvider.KEY, PageContext.REQUEST_SCOPE);
        pageContext.setAttribute(DepthProvider.KEY, new BasicDepthProvider(getDepth()), PageContext.REQUEST_SCOPE);
        collector = new ContextCollector(getContextProvider());
        tree = findTreeProvider();
        index = 0;
    }

    @Override
    public int doEndTag() throws JspTagException  {
        collector = null;
        tree     = null;
        pageContext.setAttribute(DepthProvider.KEY, prevDepthProvider, PageContext.REQUEST_SCOPE);
        prevDepthProvider = null;
        return super.doEndTag();
    }
    @Override
    public javax.servlet.jsp.jstl.core.LoopTagStatus getLoopStatus() {
        return new ListProviderLoopTagStatus(this);
    }


}

