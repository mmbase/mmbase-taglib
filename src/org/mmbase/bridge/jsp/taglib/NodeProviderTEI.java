/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;


/**
* The TEI class for NodeProviders. A NodeProvider can export one or more
* 'Nodes', and it also can export fields (if the `fields' attribute is
* given).
*
* @author Michiel Meeuwissen
**/
public class NodeProviderTEI extends TagExtraInfo {
    
    private static Logger log = Logging.getLoggerInstance(NodeProviderTEI.class.getName());    

    public NodeProviderTEI() { 
        super(); 
    }

    /**
    * implementation of TagExtraInfo return values declared here
    * should be filled at one point, currently function fillVars is responsible for
    * that and gets called before every iteration
    **/
    public VariableInfo[] getVariableInfo(TagData data){
        VariableInfo[] variableInfo =    null;
        //this method is called /before/ the values are set
        //so we can not use the data members in this class
        //but the TagData provides the necessary data
        //in effect we have to parse the data twice
        //once here and onces specific attributes are set
        //maybe this can be done better I do not know
        
        
        //The tag parameter fields defines what variables should be available
        //within the body of the tag. 
        //If an 'id' is defined as well (eg 'mynode') then this will be prefixed to the
        //fieldnames: mynode_title.
        
        int nodeVariable = 0; 
        // Number of node-variables to be defined. 
        // is 0 or 1 now, but will become more for multi-level lists (not yet ready)
        
        
        String id = "";
        Object idObject = data.getAttribute("jspvar");
        if (idObject != null){
                id = "" + idObject;
                nodeVariable = 1;
        }
        
        variableInfo =    new VariableInfo[nodeVariable];

        if (nodeVariable == 1) {
            variableInfo[0] = new VariableInfo(id,
                "org.mmbase.bridge.Node",
                true,
                VariableInfo.NESTED);
        }
        return variableInfo;
    }
        
}
