package org.jboss.windup.config.metadata;

import org.jboss.windup.graph.GraphContext;

import java.util.Collection;

/**
 * Provides a mechanism for loading {@link TechnologyReferenceTransformer} instances. Sublcasses will provide specific
 * implementations.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface TechnologyReferenceTransformerLoader
{
    Collection<TechnologyReferenceTransformer> loadTransformers(GraphContext graphContext);
}
