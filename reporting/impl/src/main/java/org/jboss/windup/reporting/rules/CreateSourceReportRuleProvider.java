package org.jboss.windup.reporting.rules;

import java.util.List;

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
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.reporting.model.MainNavigationIndexModel;
import org.jboss.windup.reporting.model.ReportFileModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.query.FindClassifiedFilesGremlinCriterion;
import org.jboss.windup.reporting.service.MainNavigationIndexModelService;
import org.jboss.windup.reporting.service.ReportModelService;
import org.jboss.windup.reporting.service.SourceReportModelService;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This creates SourceReportModel entries for every relevant item within the graph.
 * 
 * Relevancy is based on whether the item has classifications or blacklists attached to it.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class CreateSourceReportRuleProvider extends WindupRuleProvider
{
    @Inject
    private ReportModelService reportModelService;

    @Inject
    private SourceReportModelService sourceReportService;

    @Inject
    private MainNavigationIndexModelService mainNavigationIndexService;

    @Inject
    private Imported<SourceTypeResolver> resolvers;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_GENERATION;
    }

    // @formatter:off
    @Override
    public List<Class<? extends WindupRuleProvider>> getClassDependencies()
    {
        return generateDependencies(CreateMainApplicationReportRuleProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        /*
         * Find all files for which there is at least one classification or blacklist
         */
        Condition finder = Query.find(FileModel.class)
                    .piped(new FindClassifiedFilesGremlinCriterion());

        GraphOperation addSourceReport = new AbstractIterationOperation<FileModel>(FileModel.class)
        {
            public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
            {
                SourceReportModel sm = sourceReportService.create();
                ProjectModel projectModel = payload.getProjectModel();
                MainNavigationIndexModel mainNavigationIndexModel = mainNavigationIndexService
                            .getNavigationIndexForProjectModel(projectModel);
                ReportFileModel reportFileModel = GraphService.addTypeToModel(event.getGraphContext(), payload,
                            ReportFileModel.class);
                sm.setSourceFileModel(reportFileModel);
                sm.setReportName(payload.getPrettyPath());
                sm.setSourceType(resolveSourceType(payload));
                sm.setMainNavigationIndexModel(mainNavigationIndexModel);
                reportModelService.setUniqueFilename(sm, payload.getFileName(), "html");
            }
        };

        return ConfigurationBuilder.begin()
            .addRule()
            .when(finder)
            .perform(
                Iteration.over()
                .perform(addSourceReport).endIteration()
            );
    }
    // @formatter:on


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
