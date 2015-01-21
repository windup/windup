package org.jboss.windup.graph;

import java.nio.file.Path;

/**
 * Responsible for creating new {@link GraphContext} instances.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface GraphContextFactory
{
    /**
     * Create a new {@link GraphContext} using the given {@link Path} as a file storage location. The {@link Path} will
     * be created if it does not already exist. (<b>**WARNING**: This will potentially delete all data in the given
     * directory.</b>)
     */
    GraphContext create(Path dir);

    /**
     * Create a new {@link GraphContext} using a temporary file storage location.
     */
    GraphContext create();
    
    /**
     * Loads a {@link GraphContext} using the given {@link Path} as a file storage location. 
     */
    GraphContext load(Path dir);

}
