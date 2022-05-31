package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ArchiveType;
import org.jboss.windup.graph.model.TypeValue;

@TypeValue(WarArchiveModel.TYPE)
@ArchiveType(".war")
public interface WarArchiveModel extends ArchiveModel {
    String TYPE = "WarArchiveModel";
}
