package org.jboss.windup.graph.traversal;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Function;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.logging.Logger;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.DuplicateProjectModel;
import org.jboss.windup.graph.service.ProjectService;

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
 *
 *  Then for root.ear, this will only iterate WEB-INF/lib/duplicated.jar and duplicated.war.
 * </p>
 * <p>
 *     This is most useful to summarize what part of the application will be migrated by migrating the shared libraries.
 * </p>
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class SharedLibsTraversalStrategy implements TraversalStrategy
{
    public static final Logger LOG = Logger.getLogger(SharedLibsTraversalStrategy.class.getName());

    // maintains a Set of all archive hashes found so far
    private Set<String> alreadySeenHashes;


    public SharedLibsTraversalStrategy()
    {
        reset();
    }

    @Override
    public void reset()
    {
        this.alreadySeenHashes = new HashSet<>();
    }

    @Override
    public Iterable<ProjectModelTraversal> getChildren(final ProjectModelTraversal traversal)
    {
        ProjectModel canonicalProject = traversal.getCanonicalProject();

        Iterable<ProjectModelTraversal> defaultChildren = Iterables.transform(canonicalProject.getChildProjects(), new Function<ProjectModel, ProjectModelTraversal>()
        {
            @Override
            public ProjectModelTraversal apply(ProjectModel input)
            {
                return new ProjectModelTraversal(traversal, input, SharedLibsTraversalStrategy.this);
            }
        });

        return Iterables.filter(defaultChildren, new Predicate<ProjectModelTraversal>()
        {
            @Override
            public boolean apply(ProjectModelTraversal input)
            {
                final ProjectModel project = input.getCurrent();
                FileModel rootFile = project.getRootFileModel();

                LOG.info("    TRYING " + rootFile.getFilePath() + " - " + rootFile);

                // We only want the duplicated projects,
                if (!(project instanceof DuplicateProjectModel))
                    return false;
                LOG.info("    ...OK, duplicated project");///

                // We only want the duplicated archives,
                if (!(rootFile instanceof DuplicateArchiveModel))
                    return false;
                LOG.info("    ...OK, duplicated archive");///

                // which are shared between apps.
                if (!ProjectService.SHARED_LIBS_UNIQUE_ID.equals(input.getCanonicalProject().getRootProjectModel().getUniqueID()))
                    return false;
                LOG.info("    ...OK, shared");///

                final boolean added = alreadySeenHashes.add(rootFile.getSHA1Hash());
                LOG.info("    ...Added: " + added);///

                //  but only once within 1 app.
                return added;
            }
        });
    }
}
