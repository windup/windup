package org.jboss.windup.addon.reporting.meta;

import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.jboss.windup.graph.model.resource.Resource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ReportResource")
public interface ReportResource extends Resource
{
    @Adjacency(label="childReport", direction=Direction.IN)
    public ApplicationReportResource getApplicationReport();
    
    @Adjacency(label="childReport", direction=Direction.IN)
    public void setApplicationReport(ApplicationReportResource archive);

    @Adjacency(label="archiveResource", direction=Direction.OUT)
    public void setApplicationArchive(ArchiveResource archiveResource);
    
    @Adjacency(label="archiveResource", direction=Direction.OUT)
    public void getApplicationArchive();
}
