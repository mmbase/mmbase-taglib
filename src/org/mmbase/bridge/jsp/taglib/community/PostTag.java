/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.community;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTag;

import java.util.Hashtable;

import org.mmbase.bridge.Cloud;
import org.mmbase.bridge.Module;
import org.mmbase.bridge.Node;
import org.mmbase.bridge.NodeManager;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

import org.mmbase.bridge.jsp.taglib.*;

/**
 * This tag posts a message. The body of the tag is the message text.
 *
 * @author Pierre van Rooden
 */
public class PostTag extends AbstractNodeProviderTag implements BodyTag {

    private static Logger log = Logging.getLoggerInstance(PostTag.class.getName());

    private Module community=null;
    private String jspvar=null;

    public void setJspvar(String v) {
        jspvar = v;
    }

    public int doStartTag() throws JspTagException{
        community=getCloudContext().getModule("communityprc");
        // create a temporary message node that holds the new data
        Node node = new MessageNode(getCloud());
        setNodeVar(node);
        return EVAL_BODY_TAG;
    }

    public void doInitBody() throws JspTagException {
    }

    /**
     * store the given value
     **/
    public int doAfterBody() throws JspTagException {
        Node node=getNodeVar();
        String body=node.getStringValue("body").trim();
        if (body.length()==0) {
            throw new JspTagException("Field 'body' not specified");
        }
        String channel=node.getStringValue("channel");
        if (channel.length()==0) {
            throw new JspTagException("Field 'channel' not specified");
        }
        String thread=node.getStringValue("thread");
        if (thread.length()==0) { // thread not null
            thread=channel;
        }
        Hashtable params=new Hashtable();
        try {
            Cloud cloud=getCloud();
            params.put("CLOUD",cloud);
        } catch (JspTagException e) {}
        params.put("MESSAGE-BODY",body);
        params.put("MESSAGE-CHANNEL",channel);
        String user=node.getStringValue("user");
        if (user.length()!=0) params.put("MESSAGE-CHATTER",user);
        String username=(String)node.getValue("username");
        if (username!=null) params.put("MESSAGE-CHATTERNAME",username.trim());
        String subject=(String)node.getValue("subject");
        if (subject!=null) params.put("MESSAGE-SUBJECT",subject.trim());
        community.process("MESSAGE-POST",thread,params,
                          pageContext.getRequest(),pageContext.getResponse());
        Object resultvalue=params.get("MESSAGE-NUMBER");
        Object err=params.get("MESSAGE-ERROR");
        if (err!=null) {
            throw new JspTagException("Post failed : "+err);
        }
        if (jspvar!=null) {
            pageContext.setAttribute(jspvar, resultvalue.toString());
        }
        try {
            bodyContent.writeOut(bodyContent.getEnclosingWriter());
        } catch (java.io.IOException ioe){
            throw new JspTagException(ioe.toString());
        }
        return SKIP_BODY;
    }

}
