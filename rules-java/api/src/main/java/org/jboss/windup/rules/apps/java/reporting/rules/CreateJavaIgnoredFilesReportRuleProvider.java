package org.jboss.windup.rules.apps.java.reporting.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.rules.apps.java.model.IgnoredFilesReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.util.Util;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report for all the ignored files along with all the regexes they were matched against.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateJavaIgnoredFilesReportRuleProvider extends AbstractRuleProvider
{
    public static final String TITLE = "Ignored Files";
	public static final String TEMPLATE_REPORT = "/reports/templates/ignored_files.ftl";
    public static final String DESCRIPTION = "This report lists the files in the application that have not been processed based on certain rules and the " +
        Util.WINDUP_BRAND_NAME_LONG + 
        " configuration. See the '--userIgnorePath' option in the User Guide.";

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        AbstractIterationOperation<WindupJavaConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupJavaConfigurationModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupJavaConfigurationModel payload)
            {
                WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                for (FileModel inputPath : configurationModel.getInputPaths())
                {
                    ProjectModel application = inputPath.getProjectModel();
                    createIgnoredFilesReport(event.getGraphContext(), payload, application);
                }
            }

            @Override
            public String toString()
            {
                return "CreateJavaApplicationOverviewReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(
                        Query.fromType(WindupJavaConfigurationModel.class)
                    )
                    .perform(addApplicationReport);

    }

    // @formatter:on

    private void createIgnoredFilesReport(GraphContext context,
                WindupJavaConfigurationModel javaCfg, ProjectModel application)
    {
        GraphService<IgnoredFileModel> ignoredFilesModelService = new GraphService<>(context,
                IgnoredFileModel.class);

        List<IgnoredFileModel> ignoredFileModelsInApplication = new ArrayList<>();

        for (IgnoredFileModel file : ignoredFilesModelService.findAll())
        {
            Set<ProjectModel> fileApplications = ProjectTraversalCache.getApplicationsForProject(context, file.getProjectModel());
            if (fileApplications.contains(application))
            {
                ignoredFileModelsInApplication.add(file);
            }
        }

        // Do not create the report if there are no ignored files
        if (ignoredFileModelsInApplication.isEmpty())
            return;

        GraphService<IgnoredFilesReportModel> ignoredFilesService = new GraphService<>(context, IgnoredFilesReportModel.class);
        IgnoredFilesReportModel ignoredFilesReportModel = ignoredFilesService.create();
        ignoredFilesReportModel.setReportPriority(9000);
        ignoredFilesReportModel.setReportName(TITLE);
        ignoredFilesReportModel.setDescription(DESCRIPTION);
        ignoredFilesReportModel.setMainApplicationReport(false);
        ignoredFilesReportModel.setDisplayInApplicationReportIndex(true);
        ignoredFilesReportModel.setReportIconClass("glyphicon glyphicon-eye-close");
        ignoredFilesReportModel.setProjectModel(application);
        ignoredFilesReportModel.setTemplatePath(TEMPLATE_REPORT);
        ignoredFilesReportModel.setTemplateType(TemplateType.FREEMARKER);

        for (IgnoredFileModel ignoredFileModel : ignoredFileModelsInApplication)
        {
            ignoredFilesReportModel.addIgnoredFile(ignoredFileModel);
        }


        for (IgnoredFileRegexModel ignoreRegexModel : javaCfg.getIgnoredFileRegexes())
        {
            ignoredFilesReportModel.addFileRegex(ignoreRegexModel);
        }

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(ignoredFilesReportModel, "ignoredfiles_" + application.getName(), "html");
    }

}
