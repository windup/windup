package org.jboss.windup.reporting;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.meta.ApplicationReportModel;
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
                    .when(GraphSearchConditionBuilder.create("reports").ofType(ApplicationReportModel.class))
                    .perform(
                                Iteration.over("reports").var("report")
                                            .perform(FreeMarkerIterationOperation.create("report"))
                                            .endIteration()
                    );
    }

    public String getOutputFilename()
    {
        return "testapplicationreport.html";
    }
}
