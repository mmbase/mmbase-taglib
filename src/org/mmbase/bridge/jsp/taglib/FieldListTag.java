/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.Field;
import org.mmbase.bridge.Node;
import org.mmbase.bridge.FieldIterator;
import org.mmbase.bridge.FieldList;
import org.mmbase.bridge.NodeManager;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * This class makes a tag which can list the fields of a NodeManager.
 *
 * @author Michiel Meeuwissen
 *
 */
public class FieldListTag extends FieldReferrerTag implements ListProvider, FieldProvider {

    private static Logger log = Logging.getLoggerInstance(FieldListTag.class.getName());

    private FieldList     returnList;
    private FieldIterator returnValues;
    private Field         currentField;
    private int           currentItemIndex= -1;

    private String nodeManagerString = null;
    private NodeProvider nodeProvider = null;

    private int type = NodeManager.ORDER_NONE;

    public int size(){
        return returnList.size();
    }
    public int getIndex() {
        return currentItemIndex;
    }

    public Object getCurrent() {
        return currentField;
    }

    public boolean isChanged() {
        return true;
    }

    public Field getFieldVar() {
        return currentField;
    }

    public void setNodetype(String t) throws JspTagException {
        nodeManagerString = getAttributeValue(t);
    }

    public void setType(String t) throws JspTagException {
        if("create".equals(t)) {
            type = NodeManager.ORDER_CREATE;
        } else if ("edit".equals(t)) {
            type = NodeManager.ORDER_EDIT;
        } else if ("list".equals(t)) {
            type = NodeManager.ORDER_LIST;
        } else if ("search".equals(t)) {
            type = NodeManager.ORDER_SEARCH;
        } else if ("all".equals(t)) {
            type = NodeManager.ORDER_NONE;
        } else {
            throw new JspTagException("Unknown field order type " + t);
        }
    }

    private java.util.List fields = null;

    public void setFields(String f) throws JspTagException {
        fields = org.mmbase.bridge.jsp.taglib.util.StringSplitter.split(getAttributeValue(f));
    }

    public NodeProvider getNodeProvider() {
        return nodeProvider;
    }

    public Node getNodeVar() throws JspTagException {
        /*
        if (nodeManagerString != null) {
            return null;
        }
        */
        nodeProvider = findNodeProvider(false);
        if (nodeProvider == null) return null;
        return nodeProvider.getNodeVar();
    }


    public void setModified() {
        if (nodeProvider != null) {
            nodeProvider.setModified();
        }
    }

    /**
    *
    **/
    public int doStartTag() throws JspTagException{
        if (getReferid() != null) {
            if (nodeManagerString != null || type != NodeManager.ORDER_NONE) {
                throw new JspTagException("Cannot specify referid attribute together with nodetype/type attributes");
            }
            Object o =  getObject(getReferid());
            if (! (o instanceof FieldList)) {
                throw new JspTagException("Context variable " + getReferid() + " is not a FieldList");
            }
            returnList = (FieldList) o;
        } else {
            NodeManager nodeManager;

            if (nodeManagerString == null) { // living as NodeReferrer
                nodeManager = getNodeVar().getNodeManager();
            } else {
                nodeManager = getCloud().getNodeManager(nodeManagerString);
            }

            if (type != NodeManager.ORDER_NONE) {
                returnList = nodeManager.getFields(type);
                if (fields != null) {
                    throw new JspTagException ("Cannot specify fields and type attribute both at the same time. Fields = " + fields + " type = " + type);
                }

            } else {
                returnList = nodeManager.getFields();
                if (fields != null) {
                    returnList.clear();
                    java.util.Iterator i = fields.iterator();
                    while (i.hasNext()) {
                        returnList.add(nodeManager.getField((String) i.next()));
                    }
                }
            }
        }
        returnValues = returnList.fieldIterator();

        //this is where we do the search
        currentItemIndex= -1;  // reset index

        // if we get a result from the query
        // evaluate the body , else skip the body
        if (returnValues.hasNext())
            return EVAL_BODY_BUFFERED;
        return SKIP_BODY;
    }

    public int doAfterBody() throws JspTagException {
        if (getId() != null) {
            getContextTag().unRegister(getId());
        }
        if (returnValues.hasNext()){
            doInitBody();
            return EVAL_BODY_AGAIN;
        } else {
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


    public int doEndTag() throws JspTagException {
        if (getId() != null) {
            getContextTag().register(getId(), returnList);
        }
        return  EVAL_PAGE;
    }

    public void doInitBody() throws JspTagException {
        if (returnValues.hasNext()){
            currentItemIndex ++;
            currentField = returnValues.nextField();
            if (getId() != null) {
                getContextTag().register(getId(), currentField);
            }
        }
    }
}

