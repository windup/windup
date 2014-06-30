package org.jboss.windup.reporting;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class FreeMarkerOperationConfigurationProvider extends WindupConfigurationProvider
{

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_RENDERING;
    }

    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .perform(
                                FreeMarkerOperation.create("/reports/templates/FreeMarkerOperationTest.ftl",
                                            getOutputFilename())
                    );
    }

    public String getOutputFilename()
    {
        return "testapplicationreport.html";
    }
}
