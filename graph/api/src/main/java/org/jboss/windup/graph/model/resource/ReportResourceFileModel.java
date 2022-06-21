package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.model.TypeValue;

/**
 * Indicates that a file is binary (such as image).
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(ReportResourceFileModel.TYPE)
public interface ReportResourceFileModel extends FileModel {
    String TYPE = "ReportResourceFileModel";

    /**
     * Returns the path of this file within the parent project (format suitable for reporting)
     * Uses fully qualified class name notation for classes
     */
    default String getPrettyPathWithinProject(boolean useFQNForClasses) {
        // TODO: Fix this
        return "resources/" + this.getPrettyPath();
    }
}
