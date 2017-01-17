package org.jboss.windup.rules.apps.javaee.model.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.util.Logging;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public class ProjectTechnologiesStatsService extends GraphService<ProjectTechnologiesStatsModel>
{
    private final static Logger LOG = Logging.get(TechnologiesStatsService.class);

    private Set<ProjectModel> projects;
    private TechnologiesStatsService technologiesStatsService;

    public ProjectTechnologiesStatsService(GraphContext context)
    {
        super(context, ProjectTechnologiesStatsModel.class);
        this.technologiesStatsService = new TechnologiesStatsService(context);

        ProjectService projectService = new ProjectService(context);
        this.projects = projectService.getRootProjectModels();
    }

    /**
     * Compute the stats for this execution.
     */
    public Collection<ProjectTechnologiesStatsModel> computeStats()
    {
        List<ProjectTechnologiesStatsModel> result = new ArrayList<>();

        Map<ProjectModel, Map<String, Integer>> suffixToCount = this.technologiesStatsService.countFilesBySuffix();
        Map<ProjectModel, Map<String, Integer>> technologiesUsage = this.technologiesStatsService.countTechnologiesUsage();

        for (ProjectModel rootProject : this.projects)
        {
            ProjectTechnologiesStatsModel stats = this.create();
            stats.setComputed(new Date());
            stats.setProjectModel(rootProject);
            stats.setTechnologiesStatsModel(this.technologiesStatsService.computeStats(
                        suffixToCount.get(rootProject),
                        technologiesUsage.get(rootProject)));
            result.add(stats);
        }

        this.commit();

        return result;
    }
}
