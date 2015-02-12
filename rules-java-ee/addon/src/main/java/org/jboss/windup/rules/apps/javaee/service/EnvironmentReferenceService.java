package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.EnvironmentReferenceModel;

import com.tinkerpop.blueprints.GraphQuery;

/**
 * Manages creating, querying, and deleting {@link EnvironmentReferenceModel}s.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class EnvironmentReferenceService extends GraphService<EnvironmentReferenceModel>
{
    public EnvironmentReferenceService(GraphContext context)
    {
        super(context, EnvironmentReferenceModel.class);
    }

    public EnvironmentReferenceModel findEnvironmentReference(String name, String type)
    {
        GraphQuery query = getTypedQuery().has(EnvironmentReferenceModel.NAME, name).has(
                    EnvironmentReferenceModel.REFERENCE_TYPE, type);
        return getUnique(query);
    }
}
