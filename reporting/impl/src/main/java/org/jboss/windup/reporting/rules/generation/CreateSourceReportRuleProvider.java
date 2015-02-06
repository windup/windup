package org.jboss.windup.reporting.rules.generation;

import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.PostReportGeneration;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.SourceTypeResolver;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.FreeMarkerSourceReportModel;
import org.jboss.windup.reporting.model.ReportFileModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.query.FindSourceReportFilesGremlinCriterion;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.SourceReportModelService;
import org.jboss.windup.util.Logging;
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
    private static Logger LOG = Logging.get(CreateSourceReportRuleProvider.class);
    private static final String TEMPLATE = "/reports/templates/source.ftl";

    @Inject
    private Imported<SourceTypeResolver> resolvers;

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return PostReportGeneration.class;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        /*
         * Find all files for which there is at least one classification or blacklist
         */
        Condition finder = Query.fromType(SourceFileModel.class).piped(new FindSourceReportFilesGremlinCriterion());

        GraphOperation addSourceReport = new AbstractIterationOperation<FileModel>()
        {
            public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
            {
                SourceReportModelService sourceReportModelService = new SourceReportModelService(
                            event.getGraphContext());
                SourceReportModel sm = sourceReportModelService.create();
                ReportFileModel reportFileModel = GraphService.addTypeToModel(event.getGraphContext(), payload,
                            ReportFileModel.class);
                sm.setSourceFileModel(reportFileModel);
                if (reportFileModel.getProjectModel() == null)
                {
                    LOG.warning("Error, source report created for file: " + payload.getFilePath() + ", but this file does not have a " + 
                                ProjectModel.class.getSimpleName() + " associated. Execution will continue, however the source report " + 
                                "for this file may be malformed");
                }
                
                sm.setReportName(payload.getPrettyPath());
                sm.setSourceType(resolveSourceType(payload));

                sm.setReportName(payload.getFileName());
                sm.setTemplatePath(TEMPLATE);
                sm.setTemplateType(TemplateType.FREEMARKER);
                ApplicationReportService applicationReportService = new ApplicationReportService(event.getGraphContext());
                ApplicationReportModel mainAppReport = applicationReportService.getMainApplicationReportForFile(payload);
                if (mainAppReport != null) {
                    sm.setParentReport(mainAppReport);
                }
                
                GraphService.addTypeToModel(event.getGraphContext(), sm, FreeMarkerSourceReportModel.class);
                ReportService reportService = new ReportService(event.getGraphContext());
                reportService.setUniqueFilename(sm, payload.getFileName(), "html");
            }

            @Override
            public String toString()
            {
                return "AddSourceReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(finder)
                    .perform(addSourceReport);
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
