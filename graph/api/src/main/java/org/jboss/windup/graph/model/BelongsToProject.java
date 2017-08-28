package org.jboss.windup.graph.model;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public interface BelongsToProject
{

    /**
     * Gets all root project models for current model (This will be mostly 1, but there are few exceptions which have multiple project models, so it
     * returns Iterable to keep interface consistent)
     *
     * @return root project models
     */
    Iterable<ProjectModel> getRootProjectModels();


    default ProjectModel getCanonicalProjectModel(ProjectModel projectModel)
    {
        ProjectModel canonicalProjectModel = projectModel;

        if (projectModel instanceof DuplicateProjectModel)
        {
            canonicalProjectModel = ((DuplicateProjectModel) projectModel).getCanonicalProject();
        }

        return canonicalProjectModel;
    }
}
