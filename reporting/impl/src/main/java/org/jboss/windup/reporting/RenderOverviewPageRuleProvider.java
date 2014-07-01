package org.jboss.windup.reporting;

import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.meta.ApplicationReportModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class RenderOverviewPageRuleProvider extends WindupConfigurationProvider
{
    private static final String VAR_APPLICATION_REPORTS = "applicationReports";
    private static final String OUTPUT_FILENAME = "index.html";
    private static final String TEMPLATE_PATH = "/reports/templates/overview.ftl";

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_RENDERING;
    }

    @Override
    public List<Class<? extends WindupConfigurationProvider>> getClassDependencies()
    {
        return generateDependencies(ApplicationReportRenderingRuleProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        FreeMarkerOperation generateReportOperation = FreeMarkerOperation.create(TEMPLATE_PATH, OUTPUT_FILENAME,
                    VAR_APPLICATION_REPORTS);

        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(GraphSearchConditionBuilder.create(VAR_APPLICATION_REPORTS).ofType(
                                ApplicationReportModel.class))
                    .perform(generateReportOperation);
    }
}
