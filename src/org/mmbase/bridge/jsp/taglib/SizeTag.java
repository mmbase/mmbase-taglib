/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;

import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.jsp.taglib.containers.NodeListContainer;
import org.mmbase.bridge.*;
import org.mmbase.storage.search.*;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspException;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;


/**
 * The size of a list.
 *
 * @author Michiel Meeuwissen
 * @version $Id: SizeTag.java,v 1.13 2003-07-29 15:08:52 michiel Exp $ 
 */

public class SizeTag extends ListReferrerTag implements Writer {

    private static Logger log = Logging.getLoggerInstance(SizeTag.class);

    private Attribute container = Attribute.NULL;

    public int doStartTag() throws JspTagException{

        helper.setTag(this);

        NodeListContainer c = (NodeListContainer) findParentTag(NodeListContainer.class, (String) container.getValue(this), false);

        if (c != null) {
            Cloud cloud = c.getCloud();
            Query count = c.getQuery().aggregatingClone();
            
            Step step = (Step) (count.getSteps().get(0));
            count.addAggregatedField(step, cloud.getNodeManager(step.getTableName()).getField("number"), AggregatedField.AGGREGATION_TYPE_COUNT);
            
            Node result = (Node) cloud.getList(count).get(0);
            helper.setValue(new Integer(result.getIntValue("number")));
        } else {
            helper.setValue(new Integer(getList().size()));
        }

        if (getId() != null) {
            getContextProvider().getContainer().register(getId(), helper.getValue());
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doAfterBody() throws JspException {
        return helper.doAfterBody();
    }

    /**
     *
     **/
    public int doEndTag() throws JspTagException {
        return helper.doEndTag();
    }

}
