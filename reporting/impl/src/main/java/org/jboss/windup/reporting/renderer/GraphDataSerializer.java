package org.jboss.windup.reporting.renderer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Used to serialize data from the graph to an {@link OutputStream} in a particular format.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface GraphDataSerializer {
    /**
     * Serialize the data to the given output stream.
     */
    void writeGraph(OutputStream outputStream) throws IOException;
}
