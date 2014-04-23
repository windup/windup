package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.report.SourceReport;
import org.jboss.windup.graph.model.resource.FileResource;
import org.jboss.windup.graph.model.resource.Resource;

public interface SourceReportDao extends BaseDao<SourceReport>
{
    public boolean hasSourceReport(Resource resource);

    public FileResource getResourceReport(Resource resource);
}
