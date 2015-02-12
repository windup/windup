package org.jboss.windup.rules.apps.java.reporting.rules;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGeneration;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;
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
 * @author mbriskar
 *
 */
public class CreateJavaIgnoredFilesReportRuleProvider extends WindupRuleProvider
{
    public static final String TITLE = "Ignored Files";
    public static final String TEMPLATE_REPORT = "/reports/templates/ignored_files.ftl";

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return ReportGeneration.class;
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

        GraphService<IgnoredFileModel> ignoredFilesModelService = new GraphService<IgnoredFileModel>(context,
                    IgnoredFileModel.class);
        Iterable<IgnoredFileModel> allIgnoredFiles = ignoredFilesModelService.findAll();
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
        return ignoredFilesReportModel;
    }

    private List<String> getAllFatherProjectPaths(ProjectModel projectModel)
    {
        List<String> paths = new ArrayList<String>();
        paths.add(projectModel.getRootFileModel().getFilePath());
        while (projectModel.getParentProject() != null)
        {
            projectModel = projectModel.getParentProject();
            paths.add(projectModel.getRootFileModel().getFilePath());
        }
        return paths;
    }
}
