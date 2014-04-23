package org.jboss.windup.graph.model.meta.report;

import org.jboss.windup.graph.model.resource.FileResource;
import org.jboss.windup.graph.model.resource.Resource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("SourceReport")
public interface SourceReport extends Resource {

    @Adjacency(label="sourceReport", direction=Direction.IN)
    public Resource getResource();

    @Adjacency(label="sourceReport", direction=Direction.IN)
    public void setResource(Resource resource);
    
    @Adjacency(label="reportFile", direction=Direction.OUT)
    public FileResource getReportFile();
    
    @Adjacency(label="reportFile", direction=Direction.OUT)
    public void setReportFile(FileResource report);
}
