package org.jboss.windup.ast.java;

import java.nio.file.Path;
import java.util.List;

import org.jboss.windup.ast.java.data.ClassReference;

/**
 * Provides a callback to indicate that processing has completed for a particular file.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface BatchASTListener
{
    /**
     * Called to indicate that processing has completed on the specified file.
     */
    void processed(Path filePath, List<ClassReference> classReferences);

    /**
     * Called on parse failures. Note that some failures will not trigger this method, due to limitations of JDT's batch API.
     */
    void failed(Path filePath, Throwable cause);
}
