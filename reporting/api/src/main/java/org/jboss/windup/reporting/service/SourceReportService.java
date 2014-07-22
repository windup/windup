package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.source.SourceReportModel;

public class SourceReportService extends GraphService<SourceReportModel>
{

    public SourceReportService()
    {
        super(SourceReportModel.class);
    }

    public SourceReportService(GraphContext context)
    {
        super(context, SourceReportModel.class);
    }
}
