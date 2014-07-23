package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.MainNavigationIndexModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Service methods for finding and creating MainnavigationIndexModel objects.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class MainNavigationIndexModelService extends GraphService<MainNavigationIndexModel>
{

    public MainNavigationIndexModelService()
    {
        super(MainNavigationIndexModel.class);
    }

    public MainNavigationIndexModelService(GraphContext context)
    {
        super(context, MainNavigationIndexModel.class);
    }

    /**
     * Returns the MainNavigationIndexModel associated with the provided ProjectModel
     */
    public MainNavigationIndexModel getNavigationIndexForProjectModel(ProjectModel projectModel)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(projectModel.asVertex());
        pipeline.in(MainNavigationIndexModel.NAVIGATION_INDEX_TO_PROJECT_MODEL);

        MainNavigationIndexModel mainNavigationIndex = null;
        if (pipeline.hasNext())
        {
            mainNavigationIndex = frame(pipeline.next());
        }
        return mainNavigationIndex;
    }
}
