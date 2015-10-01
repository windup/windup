package org.jboss.windup.rules.apps.java.model;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a source ".java" file on disk.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(JavaSourceFileModel.TYPE)
public interface JavaSourceFileModel extends AbstractJavaSourceModel
{
    String UNPARSEABLE_JAVA_CLASSIFICATION = "Unparseable Java File";
    String UNPARSEABLE_JAVA_DESCRIPTION = "This Java file could not be parsed";

    String TYPE = "JavaSourceFileModel";
}