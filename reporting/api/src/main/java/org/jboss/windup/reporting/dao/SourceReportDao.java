package org.jboss.windup.reporting.dao;

import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.reporting.meta.SourceReportModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

public interface SourceReportDao extends BaseDao<SourceReportModel>
{
    public boolean hasSourceReport(ResourceModel resource);

    public FileModel getResourceReport(ResourceModel resource);
}
