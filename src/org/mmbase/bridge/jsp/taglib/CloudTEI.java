/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagData;

/**
 * The TEI class belonging to the CloudTag and descendents
 *
 * @author Michiel Meeuwissen
 * @version $Id: CloudTEI.java,v 1.7 2005-07-28 20:52:53 nklasens Exp $ 
 */

public class CloudTEI extends TagExtraInfo {
    
    protected String cloudType() {
        return "org.mmbase.bridge.Cloud";
    }
        
    /**
     * Implementation of TagExtraInfo return values declared here
     * should be filled at one point, in this case with the CloudTag.
     **/
    public VariableInfo[] getVariableInfo(TagData data){      
        VariableInfo[] variableInfo = null;

        String jspvar  = (String) data.getAttribute("jspvar");

        if (jspvar != null) {
            variableInfo    =  new VariableInfo[2];
            variableInfo[0] =  new VariableInfo(jspvar, cloudType(), true, VariableInfo.NESTED);
            variableInfo[1] =  new VariableInfo("TAGINFO", "Integer", true, VariableInfo.NESTED);
        } else {
            variableInfo    =  new VariableInfo[1];
            variableInfo[0] =  new VariableInfo("TAGINFO", "Integer", true, VariableInfo.NESTED);
        }
        return variableInfo;
    }
        
}
