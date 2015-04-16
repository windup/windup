package org.jboss.windup.tooling;

import java.nio.file.Path;

/**
 * Allows setting the input path.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ExecutionBuilderSetInput
{
    /**
     * Sets the input path (application source directory, or application binary file).
     */
    ExecutionBuilderSetOutput setInput(Path input);
}
