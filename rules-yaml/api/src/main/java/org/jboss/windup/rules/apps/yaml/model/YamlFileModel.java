package org.jboss.windup.rules.apps.yaml.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

@TypeValue(YamlFileModel.TYPE)
public interface YamlFileModel extends FileModel, SourceFileModel {

    String TYPE = "YamlFileModel";
}
