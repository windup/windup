package org.jboss.windup.rules.apps.java.reporting.rules;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
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
import org.jboss.windup.rules.apps.java.model.IgnoredFilesReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
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
    public static final String DESCRIPTION = "This report lists the files which were found in the application,\n" +
            "                    but based on certain rules and the Windup configuration, both built-in and local,\n" +
            "                    they were not processed.\n" +
            "                    See the <code>--userIgnorePath</code> Windup option in\n" +
            "                    <a href=\"http://windup.github.io/windup/docs/latest/html/WindupUserGuide.html#command-line-arguments\"\n" +
            "                       >Windup User Guide</a>.";

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        AbstractIterationOperation<WindupJavaConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupJavaConfigurationModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupJavaConfigurationModel payload)
            {
                WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                for (FileModel inputPath : configurationModel.getInputPaths())
                {
                    ProjectModel projectModel = inputPath.getProjectModel();
                    createIgnoredFilesReport(event.getGraphContext(), payload, projectModel);
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
                WindupJavaConfigurationModel javaCfg, ProjectModel rootProjectModel)
    {
        GraphService<IgnoredFileModel> ignoredFilesModelService = new GraphService<>(context,
                IgnoredFileModel.class);
        Iterable<IgnoredFileModel> allIgnoredFiles = ignoredFilesModelService.findAll();

        // Do not create the report if there are no ignored files
        if (!allIgnoredFiles.iterator().hasNext())
            return;

        GraphService<IgnoredFilesReportModel> ignoredFilesService = new GraphService<>(context, IgnoredFilesReportModel.class);
        IgnoredFilesReportModel ignoredFilesReportModel = ignoredFilesService.create();
        ignoredFilesReportModel.setReportPriority(9000);
        ignoredFilesReportModel.setReportName(TITLE);
        ignoredFilesReportModel.setDescription(DESCRIPTION);
        ignoredFilesReportModel.setMainApplicationReport(false);
        ignoredFilesReportModel.setDisplayInApplicationReportIndex(true);
        ignoredFilesReportModel.setReportIconClass("glyphicon glyphicon-eye-close");
        ignoredFilesReportModel.setProjectModel(rootProjectModel);
        ignoredFilesReportModel.setTemplatePath(TEMPLATE_REPORT);
        ignoredFilesReportModel.setTemplateType(TemplateType.FREEMARKER);

        for (IgnoredFileModel file : allIgnoredFiles)
        {
            List<String> allProjectPaths = getAllFatherProjectPaths(file.getProjectModel());
            if (allProjectPaths.contains(rootProjectModel.getRootFileModel().getFilePath()))
            {
                ignoredFilesReportModel.addIgnoredFile(file);
            }
        }

        for (IgnoredFileRegexModel ignoreRegexModel : javaCfg.getIgnoredFileRegexes())
        {
            ignoredFilesReportModel.addFileRegex(ignoreRegexModel);
        }

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(ignoredFilesReportModel, "ignoredfiles_" + rootProjectModel.getName(), "html");
    }

    private List<String> getAllFatherProjectPaths(ProjectModel projectModel)
    {
        List<String> paths = new ArrayList<>();
        paths.add(projectModel.getRootFileModel().getFilePath());
        while (projectModel.getParentProject() != null)
        {
            projectModel = projectModel.getParentProject();
            paths.add(projectModel.getRootFileModel().getFilePath());
        }
        return paths;
    }
}
