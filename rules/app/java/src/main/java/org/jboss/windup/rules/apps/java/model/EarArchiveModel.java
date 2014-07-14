package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ArchiveType;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("EarArchiveModel")
@ArchiveType(".ear")
public interface EarArchiveModel extends ArchiveModel
{

}
