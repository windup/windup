package org.jboss.windup.config.metadata;

import org.jboss.windup.graph.GraphContext;

/**
 * Maintains information about which technologies are defined in the rulesets.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */

public interface TechnologyMetadataProvider {
    TechnologyMetadata getMetadata(GraphContext context, TechnologyReference reference);
}
