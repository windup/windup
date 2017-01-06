package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.SpringConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.stats.TechnologyKeyValuePairModel;

/**
 *  Provides methods for creating, updating, and querying {@link TechnologyKeyValuePairModel}s.
 *
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public class TechnologyKeyValuePairModelService extends GraphService<TechnologyKeyValuePairModel>
{
    public TechnologyKeyValuePairModelService(GraphContext context)
    {
        super(context, TechnologyKeyValuePairModel.class);
    }
}
