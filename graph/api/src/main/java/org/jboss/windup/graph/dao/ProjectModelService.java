package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.BlackListModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class ProjectModelService extends GraphService<ProjectModel>
{

    public ProjectModelService()
    {
        super(ProjectModel.class);
    }

    public ProjectModelService(GraphContext context)
    {
        super(context, ProjectModel.class);
    }

    public Iterable<BlackListModel> findBlackListsForProject(ProjectModel projectModel)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(projectModel);
        pipeline.in("fileToProjectModel").in("fileModel")
                    .has(WindupVertexFrame.TYPE_FIELD, Text.CONTAINS, BlackListModel.TYPE).V();

        return new FramedVertexIterable<BlackListModel>(getGraphContext().getFramed(), pipeline, BlackListModel.class);
    }

}
