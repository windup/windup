package org.jboss.windup.graph.model;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public interface BelongsToProject
{
    /**
     * Checks if current model belongs to given project model
     *
     * @param projectModel
     * @return true if model belongs to project model, otherwise false
     */
    boolean belongsToProject(ProjectModel projectModel);

    /**
     * Gets all root project models for current model (This will be mostly 1, but there are few exceptions which have multiple project models, so it
     * returns Iterable to keep interface consistent)
     *
     * @return root project models
     */
    Iterable<ProjectModel> getRootProjectModels();
}
