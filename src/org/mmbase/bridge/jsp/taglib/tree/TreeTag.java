/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.tree;


import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.*;
import org.mmbase.bridge.util.*;

import org.mmbase.bridge.*;
import java.io.IOException;
import org.mmbase.bridge.jsp.taglib.util.*;
import org.mmbase.bridge.jsp.taglib.debug.TimerTag;
import org.mmbase.bridge.jsp.taglib.*;
import org.mmbase.bridge.jsp.taglib.containers.*;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * Something like this
<pre>
  <mm:tree type="news" repeat="related,object">
    <mm:grows to="0">
       Related to the news article: <mm:field name="title" />
    </mm:grows>
    <mm:shrinks to="0">
       END
    </mm:shrinks>
    <mm:level>
     <mm:isgreaterthan value="0">
       <mm:grows>
         <ul class="<mm:write />">
       </mm:grows>
       <mm:shrinks>
         </ul>
       </mm:shrinks>
       <mm:node elementdepth="$_">
         <li><mm:field name="gui()" /></li>
       </mm:node>                
     </mm:isgreaterthan>
    </mm:level>
  </mm:tree>
</pre>
 * @author Michiel Meeuwissen
 * @since MMBase-1.7
 * @version $Id: TreeTag.java,v 1.2 2003-12-19 09:39:05 michiel Exp $
 */
public class TreeTag extends AbstractNodeProviderTag implements TreeProvider, QueryContainerReferrer  {
    private static final Logger log = Logging.getLoggerInstance(TreeTag.class);

    private TreeList     tree;
    private TreeIterator iterator;

    private int previousDepth = -1;
    private int depth = 0;
    private int nextDepth = 1;
    private Node nextNode;

    /**
     * Lists do implement ContextProvider
     */
    private   ContextCollector  collector;
    protected int timerHandle = -1;


    protected Attribute container   = Attribute.NULL;
    protected Attribute nodeManager = Attribute.NULL;
    protected Attribute role        = Attribute.NULL;
    protected Attribute searchDir   = Attribute.NULL;
    protected Attribute maxDepth    = Attribute.NULL;

    public void setContainer(String c) throws JspTagException {
        container = getAttribute(c);
    }

    public void setType(String n) throws JspTagException {
        nodeManager = getAttribute(n);
    }

    public void setRole(String r) throws JspTagException {
        role = getAttribute(r);
    }
    public void setSearchdir(String sd) throws JspTagException {
        searchDir = getAttribute(sd);
    }
    public void setMaxdepth(String md) throws JspTagException {
        maxDepth = getAttribute(md);
    }


    // ContextProvider implementation
    public ContextContainer getContextContainer() throws JspTagException {
        if (collector == null) return getContextProvider().getContextContainer(); // to make sure old-style implemntation work (which do not initialize container)
        return collector.getContextContainer();
    }


    public int size() {
        return tree.size();
    }
    public int getIndex() {
        if (nextNode == null) {
            return iterator.previousIndex(); 
        } else {
            return iterator.previousIndex() - 1;
        }
    }

    public int getIndexOffset() {
        return 1;
    }
    public boolean isChanged() {
        return true;
    }
    
    public Object getCurrent() {
        return getNodeVar();
    }

    public void remove() {
        iterator.remove();
    }

    public int getPreviousDepth() {
        return previousDepth;
    }
    public int getDepth() {
        return depth;
    }
    public int getNextDepth() {
        return nextDepth;
    }

    /**
     * Performs the search
     */
    public int doStartTag() throws JspTagException {

        collector = new ContextCollector(getContextProvider().getContextContainer());

        // serve parent timer tag:
        TagSupport t = findParentTag(TimerTag.class, null, false);
        if (t != null) {
            timerHandle = ((TimerTag)t).startTimer(getId(), getClass().getName());
        }

        if (getReferid() != null) {
            Object o =  getObject(getReferid());
            if (! (o instanceof TreeList)) {
                throw new JspTagException("Context variable " + getReferid() + " is not a TreeList");
            }
            if (nodeManager != Attribute.NULL) {
                throw new JspTagException("'orderby' attribute does not make sense with 'referid' attribute");
            }
            if (role != Attribute.NULL) {
                throw new JspTagException("'offset' attribute does not make sense with 'referid' attribute");
            }
            if (searchDir!= Attribute.NULL) {
                throw new JspTagException("'max' attribute does not make sense with 'referid' attribute");
            }

            if (getReferid().equals(getId())) { // in such a case, don't whine
                getContextProvider().getContextContainer().unRegister(getId());
            }
            tree = (TreeList) o;
        } else {
            NodeQueryContainer c = (NodeQueryContainer) findParentTag(NodeQueryContainer.class, (String) container.getValue(this), false);
            if (c == null) throw new JspTagException("Could not find surrounding NodeQuery");
            NodeQuery query = c.getNodeQuery();
            tree = new GrowingTreeList(query, new GrowingTreeList.PathElement(getCloud().getNodeManager(nodeManager.getString(this)), role.getString(this), searchDir.getString(this)), maxDepth.getInt(this, 5));

        }
        iterator = tree.treeIterator();
        
        // returnList is know, now we can serve parent formatter tag
        FormatterTag f = (FormatterTag) findParentTag(FormatterTag.class, null, false);
        if (f != null && f.wantXML()) {
            f.getGenerator().add(tree);
        }

        if (iterator.hasNext()) {
            depth         = 0;
            nextNode      = iterator.nextNode();
            nextDepth     = iterator.currentDepth();
            return EVAL_BODY_BUFFERED;
        } else {
            return SKIP_BODY;
        }


    }

    public void doInitBody() throws JspTagException {
        if (nextNode != null) {
            setNodeVar(nextNode);
            fillVars();
            previousDepth = depth;
            depth         = nextDepth;
        }
        if (iterator.hasNext()){
            nextNode      = iterator.nextNode();
            nextDepth     = iterator.currentDepth();
        } else {
            nextNode      = null;
            nextDepth     = 0;
        }
    }


    public int doAfterBody() throws JspTagException {
        super.doAfterBody();
        if (getId() != null) {
            getContextProvider().getContextContainer().unRegister(getId());
        }
        
        collector.doAfterBody();

        if (nextNode != null){
            doInitBody();
            return EVAL_BODY_AGAIN;
        } else {
            log.debug("writing body");
            if (bodyContent != null) {
                try {
                    bodyContent.writeOut(bodyContent.getEnclosingWriter());
                } catch (IOException ioe){
                    throw new TaglibException(ioe);
                }
            }
            return SKIP_BODY;
        }

    }

    public int doEndTag() throws JspTagException {
        if (getId() != null) {
            getContextProvider().getContextContainer().register(getId(), tree);
        }
        TagSupport t = findParentTag(TimerTag.class, null, false);
        if (t != null) {
            ((TimerTag)t).haltTimer(timerHandle);
        }
        return super.doEndTag();
    }




}
