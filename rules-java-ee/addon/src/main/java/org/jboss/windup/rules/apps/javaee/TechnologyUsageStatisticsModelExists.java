package org.jboss.windup.rules.apps.javaee;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This is primarily used by tests to locate a {@link TechnologyUsageStatisticsModel} based upon the given parameters.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TechnologyUsageStatisticsModelExists extends GraphCondition {
    private String technologyName;
    private Set<String> expectedTags;
    private int expectedCount;

    public TechnologyUsageStatisticsModelExists() {
    }

    public TechnologyUsageStatisticsModelExists(String technologyName, int expectedCount, Set<String> expectedTags) {
        this.technologyName = technologyName;
        this.expectedCount = expectedCount;
        this.expectedTags = expectedTags;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context) {
        TechnologyUsageStatisticsService service = new TechnologyUsageStatisticsService(event.getGraphContext());
        Iterable<TechnologyUsageStatisticsModel> models = service.findAllByProperty(TechnologyUsageStatisticsModel.NAME, this.technologyName);
//        Iterable<TechnologyUsageStatisticsModel> models = service.findAll();

        boolean result = false;
        for (TechnologyUsageStatisticsModel model : models) {
            if (matchesExpectations(model))
                result = true;
        }

        return result;
    }

    private boolean matchesExpectations(TechnologyUsageStatisticsModel model) {
        if (!StringUtils.equals(this.technologyName, model.getName()))
            return false;

        if (this.expectedCount != model.getOccurrenceCount())
            return false;

        return model.getTags().containsAll(this.expectedTags);
    }

    public String getTechnologyName() {
        return technologyName;
    }

    public Set<String> getExpectedTags() {
        return expectedTags;
    }

    public int getExpectedCount() {
        return expectedCount;
    }
}
