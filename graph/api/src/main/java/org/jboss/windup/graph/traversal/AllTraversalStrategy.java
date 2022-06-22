package org.jboss.windup.graph.traversal;

import org.jboss.windup.graph.model.ProjectModel;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * <p>
 * Implements the default traversal for {@link ProjectModelTraversal}. This version will
 * iterate through all project children, including duplicates.
 * </p>
 * <p>
 * This is useful for cases in which you want all of the projects even if you have already analyzed them. For example,
 * this might be useful of displaying a tree of the actual structure of the project, regardless of underlying duplication.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class AllTraversalStrategy implements TraversalStrategy {
    @Override
    public ProjectModelTraversal.TraversalState getTraversalState(ProjectModelTraversal traversal) {
        return ProjectModelTraversal.TraversalState.ALL;
    }

    @Override
    public Iterable<ProjectModelTraversal> getChildren(final ProjectModelTraversal traversal) {
        ProjectModel canonicalProject = traversal.getCanonicalProject();

        return Iterables.transform(canonicalProject.getChildProjects(), new Function<ProjectModel, ProjectModelTraversal>() {
            @Override
            public ProjectModelTraversal apply(ProjectModel input) {
                return new ProjectModelTraversal(traversal, input, AllTraversalStrategy.this);
            }
        });
    }


    @Override
    public void reset() {
        // No-op.
    }
}
