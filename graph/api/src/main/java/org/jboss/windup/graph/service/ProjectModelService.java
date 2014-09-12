package org.jboss.windup.graph.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;

public class ProjectModelService extends GraphService<ProjectModel>
{
    public ProjectModelService(GraphContext context)
    {
        super(context, ProjectModel.class);
    }
}
