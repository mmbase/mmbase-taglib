/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import java.io.IOException;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.Field;
import org.mmbase.bridge.Node;
import org.mmbase.bridge.FieldIterator;
import org.mmbase.bridge.FieldList;
import org.mmbase.bridge.NodeManager;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;
import org.mmbase.util.StringSplitter;

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

    private Attribute   nodeManagerAtt = Attribute.NULL;
    private NodeProvider nodeProvider = null;

    private  Attribute type = Attribute.NULL;

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
        nodeManagerAtt = getAttribute(t);
    }

    public void setType(String t) throws JspTagException {
        type = getAttribute(t);
    }
    protected int getType() throws JspTagException {
        if (type == Attribute.NULL) return NodeManager.ORDER_NONE;
        String t = type.getString(this).toLowerCase();
        if("create".equals(t)) {
           return NodeManager.ORDER_CREATE;
        } else if ("edit".equals(t)) {
            return  NodeManager.ORDER_EDIT;
        } else if ("list".equals(t)) {
            return NodeManager.ORDER_LIST;
        } else if ("search".equals(t)) {
            return NodeManager.ORDER_SEARCH;
        } else if ("all".equals(t)) {
            return  NodeManager.ORDER_NONE;
        } else {
            throw new JspTagException("Unknown field order type " + t);
        }
    }

    private Attribute fields = Attribute.NULL;

    public void setFields(String f) throws JspTagException {
        fields = getAttribute(f);
    }
    protected java.util.List getFields() throws JspTagException {
        return StringSplitter.split(getAttributeValue(fields.getString(this)));
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
            if (nodeManagerAtt != Attribute.NULL || type != Attribute.NULL) {
                throw new JspTagException("Cannot specify referid attribute together with nodetype/type attributes");
            }
            Object o =  getObject(getReferid());
            if (! (o instanceof FieldList)) {
                throw new JspTagException("Context variable " + getReferid() + " is not a FieldList");
            }
            returnList = (FieldList) o;
        } else {
            NodeManager nodeManager;

            if (nodeManagerAtt == Attribute.NULL) { // living as NodeReferrer                
                Node n = getNodeVar();
                if (n == null) throw new JspTagException("Fieldlist tag must be used either as node-referrer, or use the objecttype attribute");
                nodeManager = n.getNodeManager();
            } else {
                nodeManager = getCloud().getNodeManager(nodeManagerAtt.getString(this));
            }

            if (type != Attribute.NULL) {
                returnList = nodeManager.getFields(getType());
                if (fields != Attribute.NULL) {
                    throw new JspTagException ("Cannot specify fields and type attribute both at the same time. Fields = " + fields + " type = " + type);
                }

            } else {
                returnList = nodeManager.getFields();
                if (fields != Attribute.NULL) {
                    returnList.clear();
                    java.util.Iterator i = getFields().iterator();
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

