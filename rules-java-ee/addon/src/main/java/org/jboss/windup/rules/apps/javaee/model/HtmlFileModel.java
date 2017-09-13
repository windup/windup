package org.jboss.windup.rules.apps.javaee.model;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import java.util.logging.Logger;

@TypeValue(HtmlFileModel.TYPE)
public interface HtmlFileModel extends FileModel, SourceFileModel
{
    Logger LOG = Logger.getLogger(HtmlFileModel.class.getName());

    String TYPE = "HtmlFile";
}
