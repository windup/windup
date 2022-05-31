package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ArchiveType;
import org.jboss.windup.graph.model.TypeValue;

/**
 * Represents a .jar archive.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(JarArchiveModel.TYPE)
@ArchiveType(".jar")
public interface JarArchiveModel extends ArchiveModel {
    String TYPE = "JarArchiveModel";
}
