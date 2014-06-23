package org.jboss.windup.reporting.meta;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("Report")
public interface ReportModel extends ResourceModel
{
    @Adjacency(label = "childReport", direction = Direction.IN)
    public ApplicationReportModel getApplicationReport();

    @Adjacency(label = "childReport", direction = Direction.IN)
    public void setApplicationReport(ApplicationReportModel archive);

    @Adjacency(label = "archiveResource", direction = Direction.OUT)
    public void setApplicationArchive(ArchiveModel archiveResource);

    @Adjacency(label = "archiveResource", direction = Direction.OUT)
    public void getApplicationArchive();
}
