package org.jboss.windup.reporting.rules.rendering;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationFilter;
import org.jboss.windup.config.phase.ReportRendering;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.freemarker.FreeMarkerIterationOperation;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This renders the ApplicationReport, along with all of its subapplications via freemarker.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class RenderReportRuleProvider extends WindupRuleProvider
{
    @Inject
    private Furnace furnace;

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return ReportRendering.class;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        FreeMarkerIterationOperation reportOperation = FreeMarkerIterationOperation.create(furnace);

        return ConfigurationBuilder
            .begin()
            .addRule()
            .when(Query.fromType(ReportModel.class))
            .perform(
                        Iteration.over()
                            .when(new AbstractIterationFilter<ReportModel>()
                            {
                                @Override
                                public boolean evaluate(GraphRewrite event, EvaluationContext context, ReportModel payload)
                                {
                                    return TemplateType.FREEMARKER.equals(payload.getTemplateType());
                                }
                                
                                @Override
                                public String toString()
                                {
                                    return "ReportModel.templateType == TemplateType.FREEMARKER";
                                }
                            })
                            .perform(reportOperation)
                            .endIteration()
            );
    }
    // @formatter:on
}
