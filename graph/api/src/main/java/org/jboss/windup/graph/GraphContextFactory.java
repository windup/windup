package org.jboss.windup.graph;

import java.nio.file.Path;

/**
 * Responsible for creating new {@link GraphContext} instances.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface GraphContextFactory {
    String DEFAULT_GRAPH_SUBDIRECTORY = "graph";

    /**
     * Create a new {@link GraphContext} using the given {@link Path} as a file storage location. The {@link Path} will
     * be created if it does not already exist. (<b>**WARNING**: This will potentially delete all data in the given
     * directory.</b>)
     * <p>
     * The enableListeners flag indicates whether or not mutation listeners should be enabled. Only a single open graph can have
     * those at a time, and these should only be used for analysis runs.
     */
    GraphContext create(Path dir, boolean enableListeners);

    /**
     * Create a new {@link GraphContext} using a temporary file storage location.
     * <p>
     * The enableListeners flag indicates whether or not mutation listeners should be enabled. Only a single open graph can have
     * those at a time, and these should only be used for analysis runs.
     */
    GraphContext create(boolean enableListeners);

    /**
     * Loads a {@link GraphContext} using the given {@link Path} as a file storage location.
     */
    GraphContext load(Path dir);

    /**
     * Close all of the graphs that have been opened by this factory. This can be useful as a final
     * cleanup to insure that all resources have been successfully destroyed.
     */
    void closeAll();
}
