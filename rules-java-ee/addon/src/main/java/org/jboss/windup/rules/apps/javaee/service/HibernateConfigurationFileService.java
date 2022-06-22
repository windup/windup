package org.jboss.windup.rules.apps.javaee.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;

/**
 * Contains methods for querying, updating, and deleting {@link HibernateConfigurationFileModel}
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class HibernateConfigurationFileService extends GraphService<HibernateConfigurationFileModel> {
    public HibernateConfigurationFileService(GraphContext context) {
        super(context, HibernateConfigurationFileModel.class);
    }

    /**
     * Gets an {@link Iterable} of {@link HibernateConfigurationFileModel}s for the given {@link ProjectModel}.
     */
    public Iterable<HibernateConfigurationFileModel> findAllByApplication(final ProjectModel application) {
        List<HibernateConfigurationFileModel> results = new ArrayList<>();

        for (HibernateConfigurationFileModel model : findAll()) {
            Set<ProjectModel> modelApplications = ProjectTraversalCache.getApplicationsForProject(getGraphContext(), model.getProjectModel());
            if (modelApplications.contains(application))
                results.add(model);
        }
        return results;
    }
}
