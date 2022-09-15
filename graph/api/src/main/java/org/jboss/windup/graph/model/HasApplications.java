package org.jboss.windup.graph.model;

import java.util.List;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public interface HasApplications extends BelongsToProject {
    /**
     * Gets all root project models for current model (This will be mostly 1, but there are few exceptions which have multiple project models, so it
     * returns Iterable to keep interface consistent)
     *
     * @return root project models
     */
    List<ProjectModel> getApplications();

    default boolean belongsToProject(ProjectModel projectModel) {
        ProjectModel canonicalProjectModel = this.getCanonicalProjectModel(projectModel);

        for (ProjectModel currentProject : this.getApplications()) {
            if (currentProject.equals(canonicalProjectModel)) {
                return true;
            }
        }

        return false;
    }
}
