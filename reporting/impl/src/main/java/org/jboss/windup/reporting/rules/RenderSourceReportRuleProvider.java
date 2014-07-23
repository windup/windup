package org.jboss.windup.reporting.rules;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.freemarker.FreeMarkerIterationOperation;
import org.jboss.windup.reporting.model.FreeMarkerSourceReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.ReportModelService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * 
 * This renders all SourceReports to the output directory.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class RenderSourceReportRuleProvider extends WindupRuleProvider
{
    @Inject
    private Furnace furnace;

    @Inject
    private ReportModelService reportModelService;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_RENDERING;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        GraphOperation renderReport = new AbstractIterationOperation<SourceReportModel>(SourceReportModel.class,
                    "sourceReport")
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, final SourceReportModel payload)
            {
                payload.setReportName(payload.getSourceFileModel().getFileName());
                payload.setTemplatePath("/reports/templates/source.ftl");
                payload.setTemplateType(TemplateType.FREEMARKER);

                FreeMarkerSourceReportModel freemarkerSourceReport = GraphService.addTypeToModel(
                            event.getGraphContext(), payload,
                            FreeMarkerSourceReportModel.class);
                // update the variable with the current type information
                Iteration.setCurrentPayload(Variables.instance(event), getVariableName(), freemarkerSourceReport);
            }
        };

        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(Query.find(SourceReportModel.class).as("sourceReports"))
                    .perform(
                                Iteration.over("sourceReports")
                                            .as("sourceReport")
                                            .perform(renderReport.and(FreeMarkerIterationOperation
                                                        .create(furnace, "sourceReport"))).endIteration()
                    );
    }
    // @formatter:on
}
