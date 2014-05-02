package org.jboss.windup.addon.reporting.meta;

import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("Report")
public interface ReportModel extends ResourceModel
{
    @Property("myproperty")
    public String getMyProperty();
    @Property("myproperty")
    public void setMyProperty(String val);
    
    @Adjacency(label="childReport", direction=Direction.IN)
    public ApplicationReportModel getApplicationReport();
    
    @Adjacency(label="childReport", direction=Direction.IN)
    public void setApplicationReport(ApplicationReportModel archive);

    @Adjacency(label="archiveResource", direction=Direction.OUT)
    public void setApplicationArchive(ArchiveResourceModel archiveResource);
    
    @Adjacency(label="archiveResource", direction=Direction.OUT)
    public void getApplicationArchive();
}
