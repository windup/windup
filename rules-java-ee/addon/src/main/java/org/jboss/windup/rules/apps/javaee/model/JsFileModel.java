package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import java.util.logging.Logger;

@TypeValue(JsFileModel.TYPE)
public interface JsFileModel extends FileModel, SourceFileModel {
    Logger LOG = Logger.getLogger(JsFileModel.class.getName());

    String TYPE = "JsFile";
}
