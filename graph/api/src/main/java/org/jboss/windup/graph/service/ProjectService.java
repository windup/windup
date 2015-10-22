package org.jboss.windup.graph.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;

/**
 * Provides useful methods for querying, creating, and updating {@link ProjectModel} instances.
 */
public class ProjectService extends GraphService<ProjectModel>
{
    public ProjectService(GraphContext context)
    {
        super(context, ProjectModel.class);
    }
}
