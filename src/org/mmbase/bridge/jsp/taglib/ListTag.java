/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.mmbase.bridge.*;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * MMList, provides functionality for listing objects stored in MMBase
 * @author Kees Jongenburger
 **/
public class ListTag extends NodeLikeTag implements BodyTag {

    private static Logger log = Logging.getLoggerInstance(ListTag.class.getName());
    
    private String nodesString=null;
    private String typeString=null;
    private String whereString=null;
    private String sortedString=null;
    private String directionString=null;
    private String distinctString=null;
    private String maxString=null;
    private String searchString=null;
    private String commandString = null;

    private int    offset   = 0;
    private boolean changed = true;

    /**
     * used when nesting tags to make a differance between the variables
     * exported from the parent tag and the on expored from this tag
     *
     **/
    // private String prefixString=null; deprecated

    /**
     * private data member to hold an enumeration
     * of the values to return. this variable will be set in
     * the start tag, and will be used to fill de return variables
     * for every iteration.
     **/
    protected NodeIterator returnValues;


    /**
     * please help we use a NodeIterator to keep the Nodes used
     * for each iteration but wil still whant to know
     * in the loop if we are talking about the first/last node
     * currentItemIndex is updated by fillVars
     * listSize is set in
     * used by getSize() isFirst() and isLast()
     **/
    private int listSize = 0;
    private int currentItemIndex= -1;

    public int getIndex() {
        return currentItemIndex;
    }


    /**
     * @deprecated
     **/

    public void setPrefix(String prefix){
        //this.prefixString = prefix;
        setId(prefix);
    }

    /**
      * @return the prefix to used for this tag if the value is set then the value is returned else an empty string is returned
      * @deprecated Use getId
      **/
    public String getPrefix(){
        return getId();
    }


    public void setCommand(String command) {
        commandString = command;
    }


    /**
     * @param nodes a node or  acomma separated list of nodes
     * to fit history if the value is -1 this is defined as null
     * it is not clear to me when multiple nodes are used and a what
     * point we need to decide is the node is a number for the moment
     * the whole node part is not interpreted at all
     **/
    public void setNodes(String nodes){
	// parse/map the nodes they can be params, sessions or aliases
	// instead of just numbers
	nodesString=parseNodes(nodes);
    }

    public void setNode(String node){
	nodesString=parseNodes(node);
    }

    /**
     * @param type a comma separated list of nodeManagers
     **/
    public void setType(String type){
	this.typeString = type;
    }

    /**
     * @param where the selection query for the object we are looking for
     * the query has two syntax depending on the amount of node managers
     * inserted in the type field
     **/
    public void setWhere(String where){
        log.debug("where " + where);
	this.whereString = where;
    }

    /**
     * @param search The search parameter, determines how directionality affects the search.
     *               possible values are <code>"both"</code>, <code>"destination"</code>,
     *                      <code>"source"</code>, and <code>"all"</code>
     **/
    public void setSearch(String search){
	this.searchString = search;
    }

    /**
     * @param sorted A comma separated list of fields on witch the returned
     * nodes should be sorted
     **/
    public void setSorted(String sorted){
	this.sortedString = sorted;
        
    }

    /**
     * @param direction the selection query for the object we are looking for
     * direction
     **/
    public void setDirection(String direction){
	this.directionString = direction;
    }

    /**
     * @param distinct the selection query for the object we are looking for
     **/
    public void setDistinct(String distinct){
	this.distinctString = distinct;
    }

    /**
     * @param max the max number of values returned
     **/
    public void setMax(String max){
	this.maxString = max;
    }

    /**
     * @param offset
     **/
    public void setOffset(int o) {
	offset = o;
    }

    /**
     *
     **/
    public int doStartTag() throws JspException{
	//this is where we do the seach

	NodeList nodes = null;
        //we hope at the end of this funtion
	//this Vector is filled with nodes after that we create an enumeration of it
	//and use one for each iteration
	String nodesSearchString= (nodesString == null)? "-1" : nodesString;
	String nodeManagers = typeString;
	String searchNodes= (nodesString == null)? "-1" : nodesString;
	String searchFields= fields; // why copying it?
	String searchWhere= whereString;
	String searchSorted= sortedString;
	String searchDirection= directionString;
	String searchSearch= searchString;
	boolean searchDistinct= (distinctString != null);

	String action= "none";
        log.debug("where " + whereString);
	try {    

            if (commandString != null) {
                if (stringSplitter(typeString,",").size() != 1) { // cannot be done on multilevel
                    throw new JspException ("Cannot do a multilevel with a command");
                }
                action = "command " + commandString + " on NodeManager " + typeString;
                log.debug(action);             
                nodes = getDefaultCloud().getNodeManager(typeString).getList(commandString, null);
                if (searchSorted != null) {
                    throw new JspException ("Cannot do a sort on a command");
                }
                if (searchWhere != null) {
                    throw new JspException ("Cannot do a where on a command");
                }
                
            } 
            else if (stringSplitter(typeString,",").size() > 1){ // multilevel
		action = "multilevel search";
		nodes = getDefaultCloud().getList(nodesSearchString,nodeManagers,searchFields,searchWhere,searchSorted,searchDirection,searchSearch,searchDistinct);
	    } else {
                action = "no multilevel search";
		boolean hasSearch = (searchWhere != null);
		boolean hasNode   = (nodesString != null);
		if (hasSearch) {
                    action = "has a where-search";
		    if (hasNode){
                        action = "has a node";
			//first hack, the MMCI does not provide a search on a node
			//but this is what we need here, so we expand the query
			//to resctrict the search on the node given
			searchWhere = "(" + searchWhere +") and ( number = " + getDefaultCloud().getNode(nodesString).getNumber() + " )";
		    }
                    action = "search relations with start node (" + nodesString + ") using a search";
		    log.debug(action);
		    NodeManager nodeManager = getDefaultCloud().getNodeManager(nodeManagers);
//		    boolean direction = ("UP".equals(searchDirection))? true: false;
//		    nodes= nodeManager.getList(searchWhere,null,direction);
		    nodes= nodeManager.getList(searchWhere, searchSorted, searchDirection);
		} else if (hasNode){
		    action = "list all relations of node("+ searchNodes +") of type("+ typeString + ")";
		    log.debug(action);
                    nodes = getDefaultCloud().getNode(searchNodes).getRelatedNodes(typeString);
                    if (searchSorted != null) {
                        throw new JspException ("Cannot do a sort on a getRelatedNodes");
                    }
                    if (searchWhere != null) {
                        throw new JspException ("Cannot do a where on a getRelatedNodes");
                    }
                    // michiel: a few more of these kind of checks could do no harm

		} else {
		    action = "list all objects of type("+ typeString + ")";
		    NodeManager nodeManager = getDefaultCloud().getNodeManager(nodeManagers);
//		    boolean direction = ("UP".equals(searchDirection))? true: false;
//		    nodes= nodeManager.getList(null,null,direction);
		    nodes= nodeManager.getList(searchWhere, searchSorted, searchDirection);
		}
	    }
	} catch (NullPointerException npe){
	    showListError(npe, nodesSearchString, nodeManagers, searchNodes, searchFields, searchWhere, searchSorted,searchDirection,searchSearch,searchDistinct,maxString,action);
	}


	if (maxString != null || offset > 0) { // for the moment max can only be here because
				//there is no other way to tell the MMCI that a list sould be shorter
	    try {
		int max = (maxString == null ? nodes.size() - 1 : Integer.parseInt(maxString));
                int to = max + offset;

                listSize = nodes.size();
                if (to >= listSize) {
                    to = listSize;
                }
                if (offset >= listSize) {
                    offset = listSize - 1;
                }
                if (offset < 0) {
                    offset = 0;
                }
                nodes=nodes.subNodeList(offset, to);

                returnValues = nodes.nodeIterator();
	    } catch (NumberFormatException e){
		throw new JspException ("MAX Field in tag is not a number");
                // isn't this a little ugly?
	    }
	} else {
	    listSize = nodes.size();
	    returnValues = nodes.nodeIterator();
	}
	// if we get a result from the query
	// evaluate the body , else skip the body
	if (returnValues.hasNext())
	    return EVAL_BODY_TAG;
	return SKIP_BODY;
    }


    public int doAfterBody() throws JspException {
        if (returnValues.hasNext()){
            doInitBody();
            return EVAL_BODY_TAG;
        } else {
            try {
                bodyOut.writeOut(bodyOut.getEnclosingWriter());
            } catch (IOException ioe){
                throw new JspTagException(ioe.toString());
            }
            return SKIP_BODY;
        }

    }
    
    
    private String previousValue = null; // static voor doInitBody
    public void doInitBody() throws JspException { 
        if (returnValues.hasNext()){
            currentItemIndex ++;
            Node next = returnValues.nextNode();
            if (sortedString != null) { // then you can also ask if 'changed' the node
                // look only at first field of sorted for the /moment.
                String f = (String)stringSplitter(sortedString).get(0);
                String value = (String)next.getValue(f);
                if (previousValue !=null) {
                    if (value.equals(previousValue)) {
                        changed = false;
                    } else {
                        changed = true;
                    }
                }
                previousValue = value;                
            }
            setNodeVar(next);
            fillVars();
        }
    }
     

    private String parseNodes(String nodes) {
	// should be a StringTokenizer have to check mmci how
	// multinodes are handled
	if (nodes.startsWith("param(")) {
	    String name=nodes.substring(6,nodes.length()-1);
	    HttpServletRequest req=(HttpServletRequest)pageContext.getRequest();
	    return(req.getParameter(name));
	}
	return(nodes);
    }

    public boolean isFirst(){
	return (currentItemIndex == 0);
    }

    public boolean isChanged() {
        return changed;
    }

    public int size(){
	return listSize;
    }

    public boolean isLast(){
	return (! returnValues.hasNext());
    }

    private void showListError( Exception npe,       String nodesSearchString,
                                String nodeManagers, String searchNodes,
                                String searchFields, String searchWhere, 
                                String searchSorted, String searchDirection, 
                                String searchSearch, boolean searchDistinct,
                                String maxString,    String action             ) throws JspException {
	StringBuffer sb = new StringBuffer();
	sb.append("nodes=" + nodesSearchString);
	sb.append("\n");
	sb.append("modeManagers=" + nodeManagers);
	sb.append("\n");
	sb.append("searchNodes=" + searchNodes);
	sb.append("\n");
	sb.append("searchFields=" + searchFields);
	sb.append("\n");
	sb.append("searchWhere=" + searchWhere);
	sb.append("\n");
	sb.append("searchSorted=" + searchSorted);
	sb.append("\n");
	sb.append("searchDirection=" + searchDirection);
	sb.append("\n");
	sb.append("searchDistinct=" + searchDistinct);
	sb.append("\n");
	sb.append("searchSearch=" + searchSearch);
	sb.append("\n");
	sb.append("max=" + maxString);
	sb.append("\n");
	sb.append("Base on the input the taglib did");
	sb.append("\n");
	sb.append(action);
	sb.append("\n");
	// now do some basic stuff to find the error

	//go to each node in the nodes string and look if they exist
	if (nodesString != null){
	    Enumeration nodeList = stringSplitter(nodesString,",").elements();
	    while(nodeList.hasMoreElements()){
		String nodeString = (String)nodeList.nextElement();
		try {
		    int nodeNumber = Integer.parseInt(nodeString);
		    Node node = getDefaultCloud().getNode(nodeNumber);
		    if (node == null){
			sb.append("ERROR: node(" + nodeString +") does not exist\n");
		    }
		} catch (NumberFormatException e){
				//the node is probabely a alias
		    Node node = getDefaultCloud().getNodeByAlias(nodeString);
		    if (node == null){
			sb.append("ERROR: node with alias("+ nodeString +") does not exist\n");
		    }
		}
	    }
	}
	//take a look at the nodeManagers
	Enumeration managerList =  stringSplitter(typeString,",").elements();
	while(managerList.hasMoreElements()){
	    String managerName = (String)managerList.nextElement();
	    try {//note nodeManager sould  realy not throw a nullPointerException
		NodeManager nodeManager = getDefaultCloud().getNodeManager(managerName);
		if (nodeManager == null){
		    sb.append("ERROR: NodeManager with name("+ managerName +") does not exist\n");
		}
	    } catch (NullPointerException nullpe){
		sb.append("ERROR: NodeManager with name("+ managerName +") does not exist , nullpointer exception while debuging ARGGGG\n");
	    }
	}
	throw new JspException("MMList error\n" + sb.toString());
    }
}

