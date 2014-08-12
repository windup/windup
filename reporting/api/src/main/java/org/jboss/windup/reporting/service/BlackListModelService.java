package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.BlackListModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * This provides helper functions for finding and creating BlackListModels within the graph.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class BlackListModelService extends GraphService<BlackListModel>
{

    public BlackListModelService()
    {
        super(BlackListModel.class);
    }

    public BlackListModelService(GraphContext context)
    {
        super(context, BlackListModel.class);
    }

    /**
     * This method finds all BlackListModels for the given ProjectModel.
     */
    public Iterable<BlackListModel> findBlackListsForProject(ProjectModel projectModel)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(projectModel);
        pipeline.in("fileToProjectModel").in("fileModel")
                    .has(WindupVertexFrame.TYPE_PROP, BlackListModel.TYPE).V();

        return new FramedVertexIterable<BlackListModel>(getGraphContext().getFramed(), pipeline, BlackListModel.class);
    }
}
