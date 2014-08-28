package org.jboss.windup.reporting.rules.rendering;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.FreeMarkerOperation;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * 
 * This renders an overview page listing all applications analyzed by the current execution of windup.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class RenderOverviewPageRuleProvider extends WindupRuleProvider
{
    private static final String VAR_APPLICATION_REPORTS = "applicationReports";
    private static final String OUTPUT_FILENAME = "index.html";
    private static final String TEMPLATE_PATH = "/reports/templates/overview.ftl";

    @Inject
    private Furnace furnace;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_RENDERING;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        FreeMarkerOperation generateReportOperation = 
            FreeMarkerOperation.create(furnace, TEMPLATE_PATH, OUTPUT_FILENAME, VAR_APPLICATION_REPORTS);

        return ConfigurationBuilder.begin()
            .addRule()
            .when(Query.find(ApplicationReportModel.class).as(VAR_APPLICATION_REPORTS))
            .perform(generateReportOperation);
    }
    // @formatter:on
}
