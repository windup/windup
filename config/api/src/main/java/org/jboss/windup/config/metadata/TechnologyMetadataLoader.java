package org.jboss.windup.config.metadata;

import org.jboss.windup.graph.GraphContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface TechnologyMetadataLoader {
    TechnologyMetadata getMetadata(GraphContext context, TechnologyReference reference);
}
