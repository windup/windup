package org.jboss.windup.reporting.renderer;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Used to serialize the graph to a particular output location.
 */
public interface GraphWriter {
    /**
     * Serialize the graph to the given output directory.
     */
    void writeGraph(Path outputDirectory) throws IOException;

}
