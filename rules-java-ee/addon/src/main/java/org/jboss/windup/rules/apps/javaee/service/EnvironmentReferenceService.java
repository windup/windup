package org.jboss.windup.rules.apps.javaee.service;

import java.util.logging.Logger;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceTagType;
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
    private static final Logger LOG = Logger.getLogger(EnvironmentReferenceService.class.getName());

    protected JNDIResourceService jndiResourceService;

    /**
     * Creates a new {@link EnvironmentReferenceService}.
     */
    public EnvironmentReferenceService(GraphContext context)
    {
        super(context, EnvironmentReferenceModel.class);
        jndiResourceService = new JNDIResourceService(context);
    }

    /**
     * Finds a {@link EnvironmentReferenceModel} by name and type.
     */
    public EnvironmentReferenceModel findEnvironmentReference(String name, EnvironmentReferenceTagType type)
    {
        GraphQuery query = getTypedQuery().has(EnvironmentReferenceModel.NAME, name).has(
                    EnvironmentReferenceModel.TAG_TYPE, type);
        return getUnique(query);
    }

    /**
     * Associate a {@link EnvironmentReferenceModel} to the given {@link JNDIResourceModel}.
     */
    public void associateEnvironmentToJndi(JNDIResourceModel resource, EnvironmentReferenceModel ref)
    {
        LOG.info("Associating JNDI: " + resource + " to Environmental Ref: " + ref.getName() + ", " + ref.getReferenceId() + ", "
                    + ref.getReferenceType());
        // hook up the JNDI resource to the environment reference
        if (ref.getJndiReference() == null)
        {
            ref.setJndiReference(resource);
        }
        jndiResourceService.associateTypeJndiResource(resource, ref.getReferenceType());
    }
}
