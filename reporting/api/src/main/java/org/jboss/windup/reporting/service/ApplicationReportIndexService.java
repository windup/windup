package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ApplicationReportIndexModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Service methods for finding and creating {@link ApplicationReportIndexModel} objects.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class ApplicationReportIndexService extends GraphService<ApplicationReportIndexModel>
{
    public ApplicationReportIndexService(GraphContext context)
    {
        super(context, ApplicationReportIndexModel.class);
    }

    /**
     * Returns the {@link ApplicationReportIndexModel} associated with the provided ProjectModel
     */
    public ApplicationReportIndexModel getApplicationReportIndexForProjectModel(ProjectModel projectModel)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(projectModel.asVertex());
        pipeline.in(ApplicationReportIndexModel.APPLICATION_REPORT_INDEX_TO_PROJECT_MODEL);

        ApplicationReportIndexModel applicationReportIndex = null;
        if (pipeline.hasNext())
        {
            applicationReportIndex = frame(pipeline.next());
        }
        return applicationReportIndex;
    }
}
