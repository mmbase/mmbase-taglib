/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;
/**
 * @author Kees Jongenburger
 */
public class MMTaglibException extends Exception{
	public MMTaglibException(String error){
		super("MMTaglibException:"  + error);
	}
}
