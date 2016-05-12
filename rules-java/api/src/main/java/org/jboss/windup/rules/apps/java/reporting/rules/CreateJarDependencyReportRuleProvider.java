package org.jboss.windup.rules.apps.java.reporting.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a report of JAR dependencies.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = ReportGenerationPhase.class, id = "Create JAR Dependency Report")
public class CreateJarDependencyReportRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE = "/reports/templates/jar_report.ftl";
    public static final String REPORT_DESCRIPTION = "This report displays all JAR dependencies found within the application.";

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        // @formatter:off
        return ConfigurationBuilder.begin()
        .addRule()
        .perform(new GraphOperation()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context)
            {
                // configuration of current execution
                WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());

                int count = 0;
                for (FileModel inputPath : configurationModel.getInputPaths())
                {
                    ProjectModel projectModel = inputPath.getProjectModel();
                    createReport(event.getGraphContext(), projectModel);
                    count++;
                }

                // only create a global report if there is more than one application
                if (count > 1)
                    createGlobalReport(event.getGraphContext(), configurationModel);
            }

            @Override
            public String toString()
            {
                return "CreateRemoteServiceReport";
            }
        });
        // @formatter:on
    }

    private void addAll(Collection<ArchiveModel> projects, ProjectModel project)
    {
        FileModel rootFileModel = project.getRootFileModel();
        if (rootFileModel instanceof ArchiveModel)
        {
            ArchiveModel archiveModel = (ArchiveModel) rootFileModel;

            // only add it if it appears to be a dependency (don't add root level projects)
            if (archiveModel.getProjectModel() != null && archiveModel.getProjectModel().getParentProject() != null)
            {
                projects.add(archiveModel);
            }
        }

        for (ProjectModel child : project.getChildProjects())
            addAll(projects, child);
    }

    private void createGlobalReport(GraphContext context, WindupConfigurationModel configuration)
    {
        Collection<ArchiveModel> dependencies = new ArrayList<>();
        for (FileModel inputApplication : configuration.getInputPaths())
            addAll(dependencies, inputApplication.getProjectModel());

        ReportService reportService = new ReportService(context);
        ApplicationReportModel reportModel = createReportModel(context, dependencies);
        reportModel.setDisplayInGlobalApplicationIndex(true);
        reportService.setUniqueFilename(reportModel, "dependency_report", "html");
    }

    private void createReport(GraphContext context, ProjectModel application)
    {
        Collection<ArchiveModel> dependencies = new ArrayList<>();
        addAll(dependencies, application);

        if (dependencies.isEmpty())
            return;

        ReportService reportService = new ReportService(context);
        ApplicationReportModel reportModel = createReportModel(context, dependencies);
        reportModel.setProjectModel(application);
        reportService.setUniqueFilename(reportModel, "dependency_report_" + application.getName(), "html");
    }

    private ApplicationReportModel createReportModel(GraphContext context, Collection<ArchiveModel> dependencies)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);
        ApplicationReportModel applicationReportModel = applicationReportService.create();
        applicationReportModel.setReportPriority(120);
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setReportName("JAR Dependencies");
        applicationReportModel.setDescription(REPORT_DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-flag");
        applicationReportModel.setTemplatePath(TEMPLATE);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
        Map<String, WindupVertexFrame> data = new HashMap<>(1);
        data.put("dependencies", listService.create().addAll(dependencies));
        applicationReportModel.setRelatedResource(data);
        return applicationReportModel;
    }
}
