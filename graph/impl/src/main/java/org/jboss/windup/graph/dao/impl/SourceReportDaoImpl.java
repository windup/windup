package org.jboss.windup.graph.dao.impl;

import org.jboss.windup.graph.dao.SourceReportDao;
import org.jboss.windup.graph.model.meta.report.SourceReportModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Direction;

public class SourceReportDaoImpl extends BaseDaoImpl<SourceReportModel> implements SourceReportDao
{
    public SourceReportDaoImpl()
    {
        super(SourceReportModel.class);
    }

    public boolean hasSourceReport(ResourceModel resource)
    {
        return resource.asVertex().getVertices(Direction.OUT, "sourceReport").iterator().hasNext();
    }

    public FileModel getResourceReport(ResourceModel resource)
    {
        if (hasSourceReport(resource))
        {
            SourceReportModel report = getContext().getFramed().frame(
                        resource.asVertex().getVertices(Direction.OUT, "sourceReport").iterator().next(),
                        SourceReportModel.class);
            return report.getReportFile();
        }
        return null;
    }
}
