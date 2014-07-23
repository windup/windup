package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ApplicationReportModel;

/**
 * This class provides helpful utility methods for creating and finding ApplicationProjectModel vertices.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class ApplicationReportModelService extends GraphService<ApplicationReportModel>
{

    public ApplicationReportModelService()
    {
        super(ApplicationReportModel.class);
    }

    public ApplicationReportModelService(GraphContext context)
    {
        super(context, ApplicationReportModel.class);
    }

}
