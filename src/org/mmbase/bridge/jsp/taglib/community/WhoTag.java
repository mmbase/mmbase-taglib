/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.community;

import java.util.*;

import javax.servlet.jsp.JspTagException;

import org.mmbase.bridge.*;
import org.mmbase.bridge.jsp.taglib.*;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * Who tag, provides functionality for listing users of a channel.
 *
 * @author Pierre van Rooden
 **/
public class WhoTag extends AbstractNodeListTag {
    private static Logger log = Logging.getLoggerInstance(WhoTag.class.getName());

    private String channel= null;
    private String type   = null;

    protected Module community = null;

    public void setChannel(String channel) throws JspTagException {
        this.channel= getAttributeValue(channel);
    }

    public void setType(String type) throws JspTagException {
        this.type=getAttributeValue(type);
    }

    public int doStartTag() throws JspTagException {
        //this is where we do the seach
        // XXX: have to add some error checking too
        community=getCloudContext().getModule("communityprc");
        if (community==null) throw new JspTagException("Community module is not active");
        if (channel==null) { // must be the surrounding node
            Node n = getNode();
            channel = n.getStringValue("number");
        }

        Hashtable params=new Hashtable();
        params.put("CHANNEL",channel);
        try {
            Cloud cloud=getCloud();
            params.put("CLOUD",cloud);
        } catch (JspTagException e) {}

        if (orderby != null)   params.put("SORTFIELDS",orderby);
        if (directions !=null) params.put("SORTDIRS",  directions);
        if (offset != Attribute.NULL)        params.put("FROMCOUNT", "" + offset.getInt(this, 0));
        if (max    != Attribute.NULL)        params.put("MAX", max.getString(this));
        NodeList nodes = community.getList("WHO", params, pageContext.getRequest(), pageContext.getResponse());
        return setReturnValues(nodes,false);
    }
}

