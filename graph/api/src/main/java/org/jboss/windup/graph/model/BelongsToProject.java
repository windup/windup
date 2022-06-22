package org.jboss.windup.graph.model;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public interface BelongsToProject {
    /**
     * Checks if current model belongs to given project model
     *
     * @param projectModel
     * @return true if model belongs to project model, otherwise false
     */
    boolean belongsToProject(ProjectModel projectModel);

    default ProjectModel getCanonicalProjectModel(ProjectModel projectModel) {
        ProjectModel canonicalProjectModel = projectModel;

        if (projectModel instanceof DuplicateProjectModel) {
            canonicalProjectModel = ((DuplicateProjectModel) projectModel).getCanonicalProject();
        }

        return canonicalProjectModel;
    }
}
