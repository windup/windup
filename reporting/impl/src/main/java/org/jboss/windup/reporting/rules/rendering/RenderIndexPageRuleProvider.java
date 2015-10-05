package org.jboss.windup.reporting.rules.rendering;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.FreeMarkerOperation;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * 
 * This renders an application index page listing all applications analyzed by the current execution of windup.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class RenderIndexPageRuleProvider extends AbstractRuleProvider
{
    private static final String VAR_APPLICATION_REPORTS = "applicationReports";
    private static final String OUTPUT_FILENAME = "../index.html";
    private static final String TEMPLATE_PATH = "/reports/templates/index.ftl";

    @Inject
    private Furnace furnace;

    public RenderIndexPageRuleProvider()
    {
        super(MetadataBuilder.forProvider(RenderIndexPageRuleProvider.class)
                    .setPhase(ReportRenderingPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        FreeMarkerOperation generateReportOperation = 
            FreeMarkerOperation.create(furnace, TEMPLATE_PATH, OUTPUT_FILENAME, VAR_APPLICATION_REPORTS);

        return ConfigurationBuilder.begin()
            .addRule()
            .when(Query.fromType(ApplicationReportModel.class).as(VAR_APPLICATION_REPORTS))
            .perform(generateReportOperation);
    }
    // @formatter:on
}
