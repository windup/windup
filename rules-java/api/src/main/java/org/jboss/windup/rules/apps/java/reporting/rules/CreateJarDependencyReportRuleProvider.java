package org.jboss.windup.rules.apps.java.reporting.rules;

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
import org.jboss.windup.reporting.model.JarDependenciesReportModel;
import org.jboss.windup.reporting.model.JarDependencyReportToProjectEdgeModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.JarDependenciesReportService;
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
    public static final String REPORT_NAME = "JAR Dependencies";
    public static final String TEMPLATE = "/reports/templates/jar_report.ftl";
    public static final String REPORT_DESCRIPTION = "This report displays all WAR/JAR dependencies found within the application.";

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
                return "CreateJarDependencyReport";
            }
        });
        // @formatter:on
    }

    private void addAll(GraphContext context, Map<String, ArchiveModel> archives, ProjectModel projectModel)
    {
        JarDependenciesReportService service = new JarDependenciesReportService(context);
        JarDependenciesReportModel jarDependenciesReportModel = service.create();
        Iterable<JarDependencyReportToProjectEdgeModel> edges = jarDependenciesReportModel.getProjectEdges();

        FileModel rootFileModel = projectModel.getRootFileModel();
        if (rootFileModel instanceof ArchiveModel)
        {
            ArchiveModel archiveModel = (ArchiveModel) rootFileModel;

            // only add it if it appears to be a dependency (don't add root level projects)
            if (archiveModel.getProjectModel() != null && archiveModel.getProjectModel().getParentProject() != null)
            {
                // adding edges for archive paths
                for (JarDependencyReportToProjectEdgeModel jarDependencyReportToProjectEdgeModel : edges)
                {
                    jarDependencyReportToProjectEdgeModel.setFullPath(archiveModel.getFilePath());
                }
                
                String archiveName = archiveModel.getArchiveName();
                if (!archiveName.equals(archives.containsKey(archiveName)))
                {
                    archives.put(archiveModel.getArchiveName(), archiveModel);
                }
            }
        }

        for (ProjectModel child : projectModel.getChildProjects())
            addAll(context, archives, child);
    }

    private void createGlobalReport(GraphContext context, WindupConfigurationModel configuration)
    {
        Map<String, ArchiveModel> dependencies = new HashMap<>();

        for (FileModel inputApplication : configuration.getInputPaths())
        {
            addAll(context, dependencies, inputApplication.getProjectModel());
        }
        
        ReportService reportService = new ReportService(context);
        JarDependenciesReportModel reportModel = createReportModel(context, dependencies);
        reportModel.setDisplayInGlobalApplicationIndex(true);
        reportService.setUniqueFilename(reportModel, "dependency_report", "html");
    }

    private void createReport(GraphContext context, ProjectModel application)
    {
        Map<String, ArchiveModel> dependencies = new HashMap<>();
        addAll(context, dependencies, application);

        if (dependencies.isEmpty())
            return;

        ReportService reportService = new ReportService(context);
        JarDependenciesReportModel reportModel = createReportModel(context, dependencies);
        reportModel.setProjectModel(application);
        reportService.setUniqueFilename(reportModel, "dependency_report_" + application.getName(), "html");
    }

    private JarDependenciesReportModel createReportModel(GraphContext context, Map<String, ArchiveModel> dependencies)
    {
        JarDependenciesReportService service = new JarDependenciesReportService(context);
        JarDependenciesReportModel applicationReportModel = service.create();

        applicationReportModel.setReportName(REPORT_NAME);
        applicationReportModel.setDescription(REPORT_DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-flag");
        applicationReportModel.setTemplatePath(TEMPLATE);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
        Map<String, WindupVertexFrame> data = new HashMap<>(1);
        data.put("dependencies", listService.create().addAll(dependencies.values()));
        applicationReportModel.setRelatedResource(data);
        return applicationReportModel;

    }
}
