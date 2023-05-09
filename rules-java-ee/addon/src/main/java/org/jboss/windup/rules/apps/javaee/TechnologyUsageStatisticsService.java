package org.jboss.windup.rules.apps.javaee;

import java.util.Date;
import java.util.logging.Logger;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;

import org.jboss.windup.util.Logging;

/**
 * Provides CRUD methods for accessing the {@link TechnologyUsageStatisticsModel} vertices.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TechnologyUsageStatisticsService extends GraphService<TechnologyUsageStatisticsModel> {
    /**
     * Creates an instance of this service.
     */
    public TechnologyUsageStatisticsService(GraphContext context) {
        super(context, TechnologyUsageStatisticsModel.class);
    }

    public TechnologyUsageStatisticsModel getOrCreate(ProjectModel projectModel, String technologyName) {
        Iterable<TechnologyUsageStatisticsModel> byName = findAllByProperty(TechnologyUsageStatisticsModel.NAME, technologyName);
        TechnologyUsageStatisticsModel result = null;

        for (TechnologyUsageStatisticsModel candidate : byName) {
            // in case of JAR files embedded in the analyzed application,
            // getProjectModel() returns the JAR itself so a technology won't be
            // properly assigned the to analyzed application (i.e. getRootProjectModel())
            if (candidate.getProjectModel().getRootProjectModel().equals(projectModel)) {
                result = candidate;
                break;
            }
        }

        if (result == null) {
            result = create();
            result.setComputed(new Date());
            result.setProjectModel(projectModel);
            result.setName(technologyName);
            result.setOccurrenceCount(0);
        }
        return result;
    }

}
