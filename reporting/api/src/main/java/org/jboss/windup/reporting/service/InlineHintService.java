package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.InlineHintModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * This provides helper functions for finding and creating BlackListModels within the graph.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class InlineHintService extends GraphService<InlineHintModel>
{

    public InlineHintService()
    {
        super(InlineHintModel.class);
    }

    public InlineHintService(GraphContext context)
    {
        super(context, InlineHintModel.class);
    }

    /**
     * Returns the total effort points in all of the {@link InlineHintModel}s associated with the files in this project.
     * 
     * If set to recursive, then also include the effort points from child projects.
     * 
     */
    public int getMigrationEffortPoints(ProjectModel projectModel, boolean recursive)
    {
        GremlinPipeline<Vertex, Vertex> inlineHintPipeline = new GremlinPipeline<>(projectModel.asVertex());
        inlineHintPipeline.out(ProjectModel.PROJECT_MODEL_TO_FILE).in(InlineHintModel.FILE_MODEL);
        inlineHintPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, InlineHintModel.TYPE);

        int hintEffort = 0;
        for (Vertex v : inlineHintPipeline)
        {
            hintEffort += (Integer) v.getProperty(InlineHintModel.PROPERTY_EFFORT);
        }

        if (recursive)
        {
            for (ProjectModel childProject : projectModel.getChildProjects())
            {
                hintEffort += getMigrationEffortPoints(childProject, recursive);
            }
        }
        return hintEffort;
    }
}
