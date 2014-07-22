package org.jboss.windup.reporting.rules;

import javax.inject.Inject;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.reporting.model.ReportFileModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.query.FindClassifiedFilesGremlinCriterion;
import org.jboss.windup.reporting.service.SourceReportModelService;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateSourceReportRuleProvider extends WindupRuleProvider
{
    @Inject
    private SourceReportModelService sourceReportService;

    @Inject
    private Imported<SourceTypeResolver> resolvers;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_GENERATION;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        /*
         * Find all files for which there is at least one classification or blacklist
         */
        Condition finder = Query.find(FileModel.class)
                    .piped(new FindClassifiedFilesGremlinCriterion())
                    .as("fileModels");

        GraphOperation addSourceReport = new AbstractIterationOperation<FileModel>(FileModel.class, "fileModel")
        {
            public void perform(GraphRewrite event, EvaluationContext context,
                        FileModel payload)
            {
                SourceReportModel sm = sourceReportService.create();
                ReportFileModel reportFileModel = GraphService.addTypeToModel(event.getGraphContext(), payload,
                            ReportFileModel.class);
                sm.setSourceFileModel(reportFileModel);
                sm.setReportName(payload.getPrettyPath());
                sm.setSourceType(resolveSourceType(payload));
            }
        };

        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(finder)
                    .perform(
                                Iteration.over("fileModels")
                                            .as("fileModel")
                                            .perform(addSourceReport).endIteration());
    }

    private String resolveSourceType(FileModel f)
    {
        for (SourceTypeResolver resolver : resolvers)
        {
            String resolvedType = resolver.resolveSourceType(f);
            if (resolvedType != null)
            {
                return resolvedType;
            }
        }
        return "unknown";
    }
}
