package org.jboss.windup.config.metadata;

import javax.inject.Inject;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.graph.GraphContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TechnologyMetadataProvider
{
    @Inject
    private Imported<TechnologyMetadataLoader> loaders;

    public TechnologyMetadata getMetadata(GraphContext context, TechnologyReference reference)
    {
        for (TechnologyMetadataLoader loader : loaders)
        {
            TechnologyMetadata metadata = loader.getMetadata(context, reference);
            if (metadata != null)
                return metadata;
        }
        return null;
    }
}
