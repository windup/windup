package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ArchiveType;
import org.jboss.windup.graph.model.TypeValue;

@TypeValue(EarArchiveModel.TYPE)
@ArchiveType(".ear")
public interface EarArchiveModel extends ArchiveModel {
    String TYPE = "EarArchiveModel";
}
