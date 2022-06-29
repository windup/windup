package org.jboss.windup.tooling;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Allows the client to reload data from an existing Windup report.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface GraphLoader {

    /**
     * Returns the {@link ExecutionResults} from a previous run of Windup.
     *
     * @throws IOException This will throw an IOException if the data cannot be loaded from the provided location.
     */
    ExecutionResults loadResults(Path reportDirectory) throws IOException;
}
