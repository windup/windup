package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SourceReport")
public interface SourceReportModel extends FileModel
{
    @Adjacency(label = "reportFile", direction = Direction.OUT)
    public FileModel getReportFile();

    @Adjacency(label = "reportFile", direction = Direction.OUT)
    public void setReportFile(FileModel report);
}
