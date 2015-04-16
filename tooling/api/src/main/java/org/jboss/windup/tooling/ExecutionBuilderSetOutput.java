package org.jboss.windup.tooling;

import java.nio.file.Path;

/**
 * This is the step that allows the output path to be set.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ExecutionBuilderSetOutput
{
    /**
     * Sets the output path for Windup (where the graph will be stored, and where the reports will be generated).
     */
    ExecutionBuilderSetOptionsAndProgressMonitor setOutput(Path output);
}
