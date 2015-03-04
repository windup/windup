package org.jboss.windup.reporting;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.FreeMarkerOperation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class TestFreeMarkerOperationRuleProvider extends AbstractRuleProvider
{

    @Inject
    private Furnace furnace;

    public TestFreeMarkerOperationRuleProvider()
    {
        super(MetadataBuilder.forProvider(TestFreeMarkerOperationRuleProvider.class)
                    .setPhase(ReportRenderingPhase.class));
    }

    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .perform(
                                FreeMarkerOperation.create(furnace, "/reports/templates/FreeMarkerOperationTest.ftl",
                                            getOutputFilename())
                    );
    }

    public String getOutputFilename()
    {
        return "testapplicationreport.html";
    }
}
