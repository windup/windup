package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;

import com.tinkerpop.blueprints.GraphQuery;

/**
 * Manages creating, querying, and deleting {@link EnvironmentReferenceModel}s.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class EnvironmentReferenceService extends GraphService<EnvironmentReferenceModel>
{
    protected JNDIResourceService jndiResourceService;
    
    public EnvironmentReferenceService(GraphContext context)
    {
        super(context, EnvironmentReferenceModel.class);
        jndiResourceService = new JNDIResourceService(context);
    }

    public EnvironmentReferenceModel findEnvironmentReference(String name, String type)
    {
        GraphQuery query = getTypedQuery().has(EnvironmentReferenceModel.NAME, name).has(
                    EnvironmentReferenceModel.REFERENCE_TYPE, type);
        return getUnique(query);
    }
    
    public void associateEnvironmentToJndi(GraphRewrite event, JNDIResourceModel resource, EnvironmentReferenceModel ref)
    {
        //hook up the JNDI resource to the environment reference
        if(ref.getJNDIReference() == null) {
            ref.setJNDIReference(resource);
        }
        jndiResourceService.associateTypeJndiResource(resource, ref.getReferenceType());
    }
}
