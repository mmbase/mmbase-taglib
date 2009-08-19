/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.containers;

import java.util.List;

import javax.servlet.jsp.*;

import org.mmbase.bridge.*;
import org.mmbase.bridge.jsp.taglib.util.Attribute;
import org.mmbase.bridge.jsp.taglib.CloudProvider;
import org.mmbase.bridge.util.Queries;
import org.mmbase.bridge.util.NodeQueryWrapper;
import org.mmbase.cache.CachePolicy;
import org.mmbase.storage.search.*;
//import org.mmbase.util.logging.*;

/**
 * Container cognate of RelatedNodesTag
 *
 * @author Michiel Meeuwissen
 * @since  MMBase-1.7
 * @version $Id$
 */
public class RelatedNodesContainerTag extends ListNodesContainerTag {

    //private static final Logger log = Logging.getLoggerInstance(RelatedNodesContainerTag.class);

    protected Attribute cachePolicy  = Attribute.NULL;
    protected Attribute role      = Attribute.NULL;

    /**
     * @since MMBase-1.8
     */
    public void setCachepolicy(String t) throws JspTagException {
        cachePolicy = getAttribute(t);
    }

    /**
     * @param role a role
     */
    public void setRole(String role) throws JspTagException {
        this.role = getAttribute(role, true);
    }


    public int doStartTag() throws JspTagException {
        initTag();
        prevQuery= pageContext.getAttribute(QueryContainer.KEY, QueryContainer.SCOPE);
        String cloneId = clone.getString(this);
        if (! "".equals(cloneId)) {
            query = new NodeQueryWrapper(ListNodesContainerTag.toNodeQuery(this, getContextProvider().getContextContainer().getObject(cloneId)));
            query.cloneQuery();
        } else if (getReferid() != null) {
            query = new NodeQueryWrapper(ListNodesContainerTag.toNodeQuery(this, getContextProvider().getContextContainer().getObject(getReferid())));
            if (nodeManager != Attribute.NULL || role != Attribute.NULL || searchDirs != Attribute.NULL || path != Attribute.NULL || element != Attribute.NULL) {
                throw new JspTagException("Cannot use 'nodemanager', 'role', 'searchdirs', 'path' or 'element' attributes together with 'referid'");
            }
        } else {
            Node node = getNode();
            Cloud cloud;
            {
                // prefer cloud of current page, otherwise of node
                CloudProvider cloudProvider = findCloudProvider(false);
                cloud = cloudProvider != null ? cloudProvider.getCloudVar() : node.getCloud();
            }
            query = new NodeQueryWrapper( cloud.createNodeQuery());

            Step step = query.addStep(node.getNodeManager());
            query.setAlias(step, node.getNodeManager().getName() + "0");
            if (node.getNumber() < 0) { // new node does not have relations, we know that.

                // The query must be made empty on one hand, and on the other hand it must store the inforrmation about this startNode.
                // The start-node is needed in #getNodeList(Query) when usetransaction="true"

                // This hack stores the tempory node in a constraint on owner, and can be picked up again in #getNodeList(Query)
                Queries.addConstraint(query, Queries.createMakeEmptyConstraint(query)); // to make absolutely sure nothing is found
                StepField f = query.createStepField(query.getSteps().get(0), node.getNodeManager().getField("owner"));
                Queries.addConstraint(query, query.createConstraint(f, Queries.getOperator("="), node.getStringValue("_number")));

            } else {
                query.addNode(step, node);
            }

            if (nodeManager != Attribute.NULL || role != Attribute.NULL) {

                String nodeManagerName;
                if (nodeManager == Attribute.NULL) {
                    nodeManagerName = "object";
                } else {
                    nodeManagerName = nodeManager.getString(this);
                }
                RelationStep relationStep = query.addRelationStep(cloud.getNodeManager(nodeManagerName),
                                                                  (String) role.getValue(this), (String) searchDirs.getValue(this));
                query.setNodeStep(relationStep.getNext());
                if (path != Attribute.NULL) throw new JspTagException("Should specify either 'type'/'role' or 'path' attributes on relatednodescontainer. Path=" + path + " Nodmanager=" + nodeManager + " role=" + role);
                if (element != Attribute.NULL) throw new JspTagException("'element' can only be used in combination with 'path' attribute. Element=" + element);
            } else {
                if (path == Attribute.NULL) throw new JspTagException("Should specify either 'type' or 'path' attributes on relatednodescontainer");

                List<Step> newSteps = Queries.addPath(query, (String) path.getValue(this), (String) searchDirs.getValue(this));

                if (element != Attribute.NULL) {
                    String alias = element.getString(this);
                    Step nodeStep = Queries.searchStep(newSteps, alias);
                    if (nodeStep == null) {
                        throw new JspTagException("Could not set element to '" + alias + "' (no such (new) step)");
                    }
                    query.setNodeStep(nodeStep);
                } else {
                    // default to third step (first two are the node and the relation)
                    query.setNodeStep(query.getSteps().get(2));
                }
            }
        }
        if (cachePolicy != Attribute.NULL) {
            query.setCachePolicy(CachePolicy.getPolicy(cachePolicy.getValue(this)));
        }

        if (getId() != null) { // write to context.
            getContextProvider().getContextContainer().register(getId(), query);
        }
        if (jspVar != null) {
            pageContext.setAttribute(jspVar, query);
        }
        pageContext.setAttribute(QueryContainer.KEY, query, QueryContainer.SCOPE);
        return EVAL_BODY;
    }



}
