package org.jboss.windup.config.metadata;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.graph.GraphContext;

import javax.inject.Inject;

/**
 * Manages loading of {@link TechnologyMetadata} from {@link TechnologyMetadataLoader}s within Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TechnologyMetadataProviderImpl implements TechnologyMetadataProvider {
    @Inject
    private Imported<TechnologyMetadataLoader> loaders;

    /**
     * Loads the {@link TechnologyMetadata} that is associated with the given {@link TechnologyReference}.
     */
    public TechnologyMetadata getMetadata(GraphContext context, TechnologyReference reference) {
        for (TechnologyMetadataLoader loader : loaders) {
            TechnologyMetadata metadata = loader.getMetadata(context, reference);
            if (metadata != null)
                return metadata;
        }
        return null;
    }
}
