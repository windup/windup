package org.jboss.windup.rules.apps.java.reporting.rules;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.model.resource.IgnoredResourceModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.IgnoredFilesReportModel;
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
 *
 */
public class CreateJavaIgnoredFilesReportRuleProvider extends AbstractRuleProvider
{
    public static final String TITLE = "Ignored Files";
    public static final String TEMPLATE_REPORT = "/reports/templates/ignored_files.ftl";

    public CreateJavaIgnoredFilesReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateJavaIgnoredFilesReportRuleProvider.class)
                    .setPhase(ReportGenerationPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        AbstractIterationOperation<WindupJavaConfigurationModel> addApplicationReport = new AbstractIterationOperation<WindupJavaConfigurationModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupJavaConfigurationModel payload)
            {
                WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event
                            .getGraphContext());
                ProjectModel projectModel = configurationModel.getInputPath().getProjectModel();
                createIgnoredFilesReport(event.getGraphContext(), payload, projectModel);
            }

            @Override
            public String toString()
            {
                return "CreateJavaApplicationOverviewReport";
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(IgnoredFilesReportModel.class))
                    .perform(addApplicationReport);

    }

    // @formatter:on

    private IgnoredFilesReportModel createIgnoredFilesReport(GraphContext context,
                WindupJavaConfigurationModel javaCfg, ProjectModel rootProjectModel)
    {
        GraphService<IgnoredFilesReportModel> ignoredFilesService = new GraphService<IgnoredFilesReportModel>(context, IgnoredFilesReportModel.class);
        IgnoredFilesReportModel ignoredFilesReportModel = ignoredFilesService.create();
        ignoredFilesReportModel.setReportPriority(100);
        ignoredFilesReportModel.setReportName(TITLE);
        ignoredFilesReportModel.setMainApplicationReport(false);
        ignoredFilesReportModel.setDisplayInApplicationReportIndex(true);
        ignoredFilesReportModel.setReportIconClass("glyphicon glyphicon-eye-close");
        ignoredFilesReportModel.setProjectModel(rootProjectModel);
        ignoredFilesReportModel.setTemplatePath(TEMPLATE_REPORT);
        ignoredFilesReportModel.setTemplateType(TemplateType.FREEMARKER);
        ignoredFilesReportModel.setDisplayInApplicationList(false);

        GraphService<IgnoredResourceModel> ignoredFilesModelService = new GraphService<IgnoredResourceModel>(context,
                    IgnoredResourceModel.class);
        Iterable<IgnoredResourceModel> allIgnoredFiles = ignoredFilesModelService.findAll();
        for (IgnoredResourceModel file : allIgnoredFiles)
        {
            List<String> allProjectPaths = getAllFatherProjectPaths(file.getProjectModel());
            if (allProjectPaths.contains(rootProjectModel.getRootResourceModel().getFilePath()))
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
        return ignoredFilesReportModel;
    }

    private List<String> getAllFatherProjectPaths(ProjectModel projectModel)
    {
        List<String> paths = new ArrayList<String>();
        paths.add(projectModel.getRootResourceModel().getFilePath());
        while (projectModel.getParentProject() != null)
        {
            projectModel = projectModel.getParentProject();
            paths.add(projectModel.getRootResourceModel().getFilePath());
        }
        return paths;
    }
}
