package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;

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
}
