/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;


import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.util.*;

import org.mmbase.bridge.*;

/**
 * MMBase taglib for use in combination with MMBase
 * MMTaglib is provided to prodide a scan like syntax for MMBase<BR>
 * based on ExampleTagBase from jakarta-tomcat
 * @author Kees Jongenburger
 **/

public abstract class MMTaglib extends TagExtraInfo implements Tag{
    /**
     * static Cloud context
     **/
    private static CloudContext cloudContext;
    private static Cloud defaultCloud;
    public static String DEFAULT_CLOUD_NAME = "mmbase";

    
    public MMTaglib(){
	cloudContext=LocalContext.getCloudContext();
	defaultCloud=cloudContext.getCloud(DEFAULT_CLOUD_NAME);
    }

    /**
     * @return the default cloud context 
     **/
    public CloudContext getDefaultCloudContext(){
    	if (cloudContext == null){
	    cloudContext=LocalContext.getCloudContext();
        } 
	return cloudContext;
    }
    /**
     * @return the default cloud being the cloud with name equals to the DEFAULT_CLOUD_NAME
     * defined in this class
     **/
    public Cloud getDefaultCloud(){
	if (defaultCloud == null){
	    defaultCloud= getDefaultCloudContext().getCloud(DEFAULT_CLOUD_NAME);
	} 
	return defaultCloud;
    }

    /**
     * method inherited from TagExtraInfo used to define
     * what parameters should be returned from a tag
     * this method is not implemented since every tag should return
     * what it want, still it is there so that inheriting classes
     * get a change to implement it
     **/
    public  VariableInfo[] getVariableInfo(TagData data){
	System.err.println("MMTaglib.getVariableInfo was called. this is can normaly not happend");
	System.err.println("the cause is that in the tld file you declared the class that overrides");
	System.err.println("this class to implement TagExtraInfo but did not create the function");
	return null;
    };


    /**
     * holder for the parent tag
     **/
    protected Tag parent;

    /**
     * holder for the PageContext
     **/
    protected PageContext pageContext;


    /**
     * holder for the content of the body
     **/
    protected BodyContent bodyOut;

    /**
     * holder for the DoStartTag return 
     * what to do after the endtag is called (default SKIP_BODY)
     * so the iner tags wil get evaludated
     **/
    int startTagReturnValue = Tag.SKIP_BODY;

    /**
     * holder for the doEndTag return 
     * what to do after the endtag is called (default EVAL_PAGE)
     **/
    int endTagReturnValue = Tag.EVAL_PAGE;


	
    /**
     * @return a value descbibing what to do after 
     * this method returns(SKIP_BODY|EVAL_BODY_INCLUDE|SKIP_PAGE|EVAL_PAGE) or
     * EVAL_BODY_TAG if the sub class implements BodyTag
     **/
    public int doEndTag() throws JspException{
	return endTagReturnValue;
    }

    /**
     * @return a value descbribing what to do after 
     * this method returns(SKIP_BODY|EVAL_BODY_INCLUDE|SKIP_PAGE|EVAL_PAGE) or
     * EVAL_BODY_TAG if the sub class implements BodyTag
     **/
    public int doStartTag() throws JspException{
	return startTagReturnValue;
    }


    public void setPageContext(PageContext pageContext){
	this.pageContext = pageContext;
    }

    public void release() {
	parent = null;
	pageContext = null;
    }
	
    public Tag getParent(){
	return parent;
    }

    public void setParent(Tag parent){
	this.parent = parent;
    }

    public void setBodyContent(BodyContent bodyOut) {
	this.bodyOut = bodyOut;
    }
    // Default implementations for BodyTag methods as well
    // just in case a tag decides to implement BodyTag.
    // keesj:
    // this is quite funny , implementing methods of an interface
    // for the subclasses (this class does not require these methods
    // but if a subclass decides to implement the BodyTag we are ready for it
    // class x implements test
    // class x has method y
    // class z implements interface with method y :)
    public void doInitBody() throws JspException {
    }

    public int doAfterBody() throws JspException {
	return SKIP_BODY;
    }
}
