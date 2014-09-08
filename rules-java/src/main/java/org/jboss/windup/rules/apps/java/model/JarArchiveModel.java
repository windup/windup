package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ArchiveType;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a .jar archive.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
@TypeValue("JarArchiveResource")
@ArchiveType(".jar")
public interface JarArchiveModel extends ArchiveModel
{
}
