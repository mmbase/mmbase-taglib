/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagData;

import java.util.Vector; 
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.mmbase.bridge.Node;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;


/**
 * A base class for tags which export a node (And some fields).
 *
 * @author Michiel Meeuwissen
 **/
abstract public class NodeLikeTag extends MMTaglib {

    private static Logger log = Logging.getLoggerInstance(NodeLikeTag.class.getName());

    private   Node   node;        
    protected String fields = "";


    /**
     * For use by children, they can find the currend 'node' belonging
     * to this tag.
     */

    public Node getNodeVar() {
        return node;
    }

    protected void setNodeVar (Node n) {
        node = n;
    }

    /**
     * @param fields a comma separated list of fields
     **/
    public void setFields(String f){
	fields = f;
    }


    abstract public void doInitBody() throws JspException;
  
    /**
     * implementation of TagExtraInfo return values declared here
     * should be filled at one point, currently fillVars is responsible for
     * that ant gets called before every
     **/
    public VariableInfo[] getVariableInfo(TagData data){
	VariableInfo[] variableInfo =    null;
	//this method is called /before/ the values are set
	//so we can not use the data members in this class
	//but the TagData provides the necessary data
	//in effect we have to parse the data twice
	//once here and onces specific attributes are set
	//maybe this can be done better I do not know


	//the tag parameter fields defines what variables should be available
	//within the body of the tag currently the only thing we do here
	//is return the a Virtual node and some variables
	//if the variable has dots in it they will be replaced by underscores
	// <%= fieldName %> or <%= node.getValue("fieldName") %>

        String fieldss = (String)data.getAttribute("fields");
        log.debug("fields : " + fieldss);

        if (fieldss == null) fieldss = "";

        Vector fields  = stringSplitter(fieldss, ",");


        int nodeVariable = 0; 
        // number of node-variable to be defined. 
        // is 0 or 1 now, but will become more for multi-level lists (not yet ready)

        // prefix is used when nesting tags to be able to make a difference
	// between the variable declared in the root tag ant this tag
	String id = "";
	if (data.getAttribute("id") != null){
            id = "" + data.getAttribute("id");
            nodeVariable = 1;
	}
        log.debug("id : " + id);

       
        variableInfo =    new VariableInfo[(fields.size()) + nodeVariable];
        int j = 0;
        for (int i = 0 ; i < fields.size(); i++){
            String field = (String)fields.elementAt(i);
            //it would be nice to return Integer is a field is of that type

            // michiel: I think we should deprecate also this.

            log.debug("will set " + getSimpleReturnValueName(id, field));
            variableInfo[j++] = new VariableInfo(getSimpleReturnValueName(id, field),
                                                 "java.lang.String",
                                                 true,
                                                 VariableInfo.NESTED);
            /* michiel: this does not make much sense. why would someone want to use 'item1', if you have 
               a name to use. It also clashes with a builder which has a fieldname 'item1'.
            variableInfo[j++] = new VariableInfo(prefix + "item" + (i+1),
                                                 "java.lang.String",
                                                 true,
                                                 VariableInfo.NESTED);            
            */
        }
        if (nodeVariable == 1) {
            variableInfo[j] = new VariableInfo(id,
                                               "org.mmbase.bridge.Node",
                                               true,
                                               VariableInfo.NESTED);
        }
	return variableInfo;
    }


    protected void fillVars(){    
        Enumeration returnFieldEnum = stringSplitter(fields,",").elements();
        int j=1;
        while (returnFieldEnum.hasMoreElements()){
            String field = (String)returnFieldEnum.nextElement();
            // michiel: should be deprecated?
            log.debug("will set " + getSimpleReturnValueName(field) + " to " + node.getValue(field));
            pageContext.setAttribute(getSimpleReturnValueName(field) ,
                                     "" + node.getValue(field));
            //pageContext.setAttribute(getPrefix() + "item"+(j++) ,
            //                         "" + node.getValue(field));
        }
        String id = getId();
        if (id != null && id != "") {
            pageContext.setAttribute(id, node);
        }
    }

    
    /**
     * simple util method to split comma separated values
     * to a vector
     * @param string the string to split
     * @param delimiter
     * @return a Vector containing the elements, the elements are also trimed
     **/
    protected Vector stringSplitter(String string,String delimiter){
	Vector retval = new Vector();
	StringTokenizer st = new StringTokenizer(string, delimiter);
	while(st.hasMoreTokens()){
	    retval.addElement(st.nextToken().trim());
	}
	return retval;
    }
    
    protected Vector stringSplitter(String string) {
        return stringSplitter(string, ",");
    }

    private String getSimpleReturnValueName(String fieldName){
        return getSimpleReturnValueName(getId(), fieldName);
    }
    private String getSimpleReturnValueName(String id, String fieldName){
        String field = fieldName.replace('.','_');
        if (id != null && ! "".equals(id)) {
            field = id + "_" + field;
        }
        return field;
    }


    
}

