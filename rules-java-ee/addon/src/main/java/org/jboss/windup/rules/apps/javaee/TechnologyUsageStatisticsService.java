package org.jboss.windup.rules.apps.javaee;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;

import java.util.Date;

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
            if (candidate.getProjectModel().equals(projectModel)) {
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
