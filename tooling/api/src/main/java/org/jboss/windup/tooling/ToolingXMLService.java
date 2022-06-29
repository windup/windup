package org.jboss.windup.tooling;

import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Path;

/**
 * Contains tools for managing XML, for example generating schemas or serializing {@link ExecutionResults}
 * to XML format.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ToolingXMLService extends Serializable {
    /**
     * Serialize the given results to the provided {@link OutputStream}.
     */
    void serializeResults(ExecutionResults results, OutputStream outputStream);

    /**
     * Generates the XSD schema and outputs it to the provided path (full path to a filename).
     * <p>
     * This will throw an exception if the path cannot be written to due to permissions or other IO errors.
     * If anything exists at the path, it will be overwritten!
     */
    void generateSchema(Path outputPath);
}
