package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import java.util.logging.Logger;

@TypeValue(CssFileModel.TYPE)
public interface CssFileModel extends FileModel, SourceFileModel {
    Logger LOG = Logger.getLogger(CssFileModel.class.getName());

    String TYPE = "CssFile";
}
