package org.jboss.windup.config.projecttraversal;

import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains a cache of some traversal data to prevent the need for frequent recalculation of this data.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ProjectTraversalCache extends AbstractRuleLifecycleListener {
    private static final Map<ProjectModel, SoftReference<Set<ProjectModel>>> moduleToApplicationCache = new ConcurrentHashMap<>();
    private static final Map<ProjectModel, SoftReference<Set<ProjectModel>>> applicationToProjectCache = new ConcurrentHashMap<>();

    public static Set<ProjectModel> getApplicationsForProject(GraphContext context, ProjectModel project) {
        Set<ProjectModel> results = getFromCache(project);
        if (results != null)
            return results;

        results = new HashSet<>();

        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);
        for (FileModel inputFile : configurationModel.getInputPaths()) {
            ProjectModel application = inputFile.getProjectModel();
            synchronized (applicationToProjectCache) {
                SoftReference<Set<ProjectModel>> projectsInApplicationReference = applicationToProjectCache.get(application);

                Set<ProjectModel> projectsInApplication = null;
                if (projectsInApplicationReference != null)
                    projectsInApplication = projectsInApplicationReference.get();

                if (projectsInApplication == null) {
                    ProjectModelTraversal traversal = new ProjectModelTraversal(application);
                    projectsInApplication = traversal.getAllProjects(true);
                    applicationToProjectCache.put(application, new SoftReference<>(projectsInApplication));
                }

                if (projectsInApplication.contains(project))
                    results.add(application);
            }
        }

        /*
         * HACK -- In tests it is possible for there to be no true "application". This can result
         *  in no results for this particular project.
         *
         *  In a case like this, the best that we can do is to simply return the project itself as the Set.
         */
        if (results.isEmpty())
            results.add(project);

        putInCache(project, results);

        return results;
    }

    private static Set<ProjectModel> getFromCache(ProjectModel project) {
        if (project == null)
            return null;

        synchronized (moduleToApplicationCache) {
            SoftReference<Set<ProjectModel>> referenceProjectsSet = moduleToApplicationCache.get(project);
            return referenceProjectsSet == null ? null : referenceProjectsSet.get();
        }
    }

    private static void putInCache(ProjectModel project, Set<ProjectModel> projects) {
        if (project == null)
            return;

        synchronized (moduleToApplicationCache) {
            SoftReference<Set<ProjectModel>> referenceProjectsSet = new SoftReference<>(projects);
            moduleToApplicationCache.put(project, referenceProjectsSet);
        }
    }

    @Override
    public void beforeExecution(GraphRewrite event) {
        moduleToApplicationCache.clear();
    }
}
