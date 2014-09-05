package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ApplicationReportModel;

/**
 * This class provides helpful utility methods for creating and finding {@link ApplicationReportModel} vertices.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class ApplicationReportService extends GraphService<ApplicationReportModel>
{

    public ApplicationReportService()
    {
        super(ApplicationReportModel.class);
    }

    public ApplicationReportService(GraphContext context)
    {
        super(context, ApplicationReportModel.class);
    }

}
