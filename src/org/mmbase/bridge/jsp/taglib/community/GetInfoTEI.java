/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.community;

import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;

/**
* The GetInfo tag can optionally export one jsp variable.
*
* @author Pierre van Rooden
*/
public class GetInfoTEI extends TagExtraInfo {

    public GetInfoTEI() {
        super();
    }

    public VariableInfo[] getVariableInfo(TagData data) {
        String jspvarAttribute  = (String) data.getAttribute("jspvar");
        if (jspvarAttribute==null) {
            return  new VariableInfo[0];
        } else {
            VariableInfo[] variableInfo =  new VariableInfo[1];
            variableInfo[0] =  new VariableInfo(jspvarAttribute,
                                            "java.lang.String",
                                            true,
                                            VariableInfo.NESTED);
            return variableInfo;
        }
    }
}
