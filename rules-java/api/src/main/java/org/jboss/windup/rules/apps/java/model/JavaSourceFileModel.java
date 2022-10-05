package org.jboss.windup.rules.apps.java.model;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.model.TypeValue;

/**
 * Represents a source ".java" file on disk.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(JavaSourceFileModel.TYPE)
public interface JavaSourceFileModel extends AbstractJavaSourceModel {
    ///String UNPARSEABLE_JAVA_CLASSIFICATION = "Unparseable Java File";
    ///String UNPARSEABLE_JAVA_DESCRIPTION = "This Java file could not be parsed";

    String TYPE = "JavaSourceFileModel";

    /**
     * Returns the path of this file within the parent project (format suitable for reporting)
     * Uses fully qualified class name notation for classes
     */
    default String getPrettyPathWithinProject(boolean useFQNForClasses) {
        if (!useFQNForClasses) {
            return this.getPrettyPathWithinProject();
        }

        String filename = StringUtils.removeEndIgnoreCase(getFileName(), ".java");
        String packageName = getPackageName();

        return (packageName == null || packageName.isEmpty()) ? filename : packageName + "." + filename;
    }

}
