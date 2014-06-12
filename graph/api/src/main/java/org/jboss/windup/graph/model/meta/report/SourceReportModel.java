package org.jboss.windup.graph.model.meta.report;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SourceReport")
public interface SourceReportModel extends ResourceModel {

    @Adjacency(label="sourceReport", direction=Direction.IN)
    public ResourceModel getResource();

    @Adjacency(label="sourceReport", direction=Direction.IN)
    public void setResource(ResourceModel resource);
    
    @Adjacency(label="reportFile", direction=Direction.OUT)
    public FileModel getReportFile();
    
    @Adjacency(label="reportFile", direction=Direction.OUT)
    public void setReportFile(FileModel report);
}
