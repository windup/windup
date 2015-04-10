package org.jboss.windup.tooling;

import java.nio.file.Path;

/**
 * The initial call, specifying the installation path to Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ExecutionBuilder
{
    /**
     * Start building a Windup execution with a windup that is installed at the specified path.
     */
    ExecutionBuilderSetInput begin(Path windupHome);
}
