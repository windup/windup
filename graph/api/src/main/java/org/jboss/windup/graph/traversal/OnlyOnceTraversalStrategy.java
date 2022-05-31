package org.jboss.windup.graph.traversal;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Provides a traversal that will limit the returned children to children that are not duplicated within this traversal.
 * </p>
 *
 * <p>
 * Put another way, if we have a ProjectTree that looks like this:
 *  <ul>
 *      <li>root.ear</li>
 *      <li>foo.war</li>
 *      <ul>
 *          <li>WEB-INF/lib/duplicated.jar</li>
 *          <li>WEB-INF/lib/other.jar</li>
 *      </ul>
 *      <li>duplicated.jar</li>
 *      <li>another.jar</li>
 *  </ul>
 * <p>
 *  Then this will only iterate root.ear, foo.war, duplicated.jar (one time only), WEB-INF/lib/other.jar, and another.jar.
 * </p>
 * <p>
 *     This is most useful in cases where you care about the contents of a project hierarchy but where representing them
 *     more than once would be redundant.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class OnlyOnceTraversalStrategy implements TraversalStrategy {
    private Set<String> alreadySeenHashes;

    public OnlyOnceTraversalStrategy() {
        reset();
    }

    @Override
    public ProjectModelTraversal.TraversalState getTraversalState(ProjectModelTraversal traversal) {
        return ProjectModelTraversal.TraversalState.ALL;
    }

    @Override
    public void reset() {
        this.alreadySeenHashes = new HashSet<>();
    }

    @Override
    public Iterable<ProjectModelTraversal> getChildren(final ProjectModelTraversal traversal) {
        ProjectModel canonicalProject = traversal.getCanonicalProject();

        Iterable<ProjectModelTraversal> defaultChildren = Iterables.transform(canonicalProject.getChildProjects(), new Function<ProjectModel, ProjectModelTraversal>() {
            @Override
            public ProjectModelTraversal apply(ProjectModel input) {
                return new ProjectModelTraversal(traversal, input, OnlyOnceTraversalStrategy.this);
            }
        });

        return Iterables.filter(defaultChildren, new Predicate<ProjectModelTraversal>() {
            @Override
            public boolean apply(ProjectModelTraversal input) {
                FileModel rootFile = input.getCurrent().getRootFileModel();

                // This duplicate handling logic only applies to archives, so skip if it is not an archive
                if (!(rootFile instanceof ArchiveModel))
                    return true;

                ArchiveModel archive = (ArchiveModel) rootFile;

                if (!alreadySeenHashes.contains(archive.getSHA1Hash())) {
                    alreadySeenHashes.add(archive.getSHA1Hash());
                    return true;
                } else {
                    return false;
                }
            }
        });
    }
}
