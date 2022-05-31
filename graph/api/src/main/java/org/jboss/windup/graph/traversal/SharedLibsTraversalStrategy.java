package org.jboss.windup.graph.traversal;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.ProjectService;

import java.util.logging.Logger;

/**
 * <p>
 * Provides a traversal over children that are shared across multiple applications
 * (i.e. those which are contained in the 'shared-libs' project);
 * if duplicated within the given application, it takes only one instance.
 * </p>
 *
 * <p>
 * Put another way, if we have a ProjectModel's forrest that looks like this:
 *  <ul>
 *      <li>root.ear</li>
 *          <ul>
 *              <li>WEB-INF/lib/duplicated.jar</li>
 *              <li>WEB-INF/lib/other.jar</li>
 *          </ul>
 *      </li>
 *      <li>duplicated.war</li>
 *      <li>duplicated.jar</li>
 *  </ul>
 *
 *  <ul>
 *      <li>root2.ear</li>
 *          <ul>
 *              <li>WEB-INF/lib/duplicated.jar</li>
 *              <li>WEB-INF/lib/foo.jar</li>
 *          </ul>
 *      </li>
 *      <li>duplicated.war</li>
 *  </ul>
 * <p>
 *  Then for root.ear, this will only iterate WEB-INF/lib/duplicated.jar and duplicated.war.
 * </p>
 * <p>
 *     This is most useful to summarize what part of the application will be migrated by migrating the shared libraries.
 * </p>
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class SharedLibsTraversalStrategy implements TraversalStrategy {
    public static final Logger LOG = Logger.getLogger(SharedLibsTraversalStrategy.class.getName());

    public SharedLibsTraversalStrategy() {
        reset();
    }

    @Override
    public ProjectModelTraversal.TraversalState getTraversalState(ProjectModelTraversal traversal) {
        if (ProjectService.SHARED_LIBS_UNIQUE_ID.equals(traversal.getCanonicalProject().getRootProjectModel().getUniqueID()))
            return ProjectModelTraversal.TraversalState.ALL;
        else
            return ProjectModelTraversal.TraversalState.CHILDREN_ONLY;
    }

    @Override
    public void reset() {
    }

    @Override
    public Iterable<ProjectModelTraversal> getChildren(final ProjectModelTraversal traversal) {
        ProjectModel canonicalProject = traversal.getCanonicalProject();

        Iterable<ProjectModelTraversal> defaultChildren = Iterables.transform(canonicalProject.getChildProjects(), new Function<ProjectModel, ProjectModelTraversal>() {
            @Override
            public ProjectModelTraversal apply(ProjectModel input) {
                return new ProjectModelTraversal(traversal, input, SharedLibsTraversalStrategy.this);
            }
        });

        return Iterables.filter(defaultChildren, new Predicate<ProjectModelTraversal>() {
            @Override
            public boolean apply(ProjectModelTraversal input) {
                // which are shared between apps.
                ProjectModelTraversal.TraversalState traversalState = getTraversalState(input);
                return traversalState != ProjectModelTraversal.TraversalState.NONE;
            }
        });
    }
}
