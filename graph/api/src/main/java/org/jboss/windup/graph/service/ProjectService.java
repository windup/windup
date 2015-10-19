package org.jboss.windup.graph.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;

/**
 * Provides useful methods for querying, creating, and updating {@link ProjectModel} instances.
 */
public class ProjectService extends GraphService<ProjectModel>
{
    public ProjectService(GraphContext context)
    {
        super(context, ProjectModel.class);
    }

    public ProjectModel getRootProject()
    {
        /*Iterator<ProjectModel> iterator = this.findAll().iterator();
        if (!iterator.hasNext())
            throw new WindupException("No projects found, can't find the root one.");

        return iterator.next().getRootProjectModel();
        */
        return new GraphService<>(getGraphContext(), WindupConfigurationModel.class).getUnique().getInputPath().getProjectModel();
    }
}
