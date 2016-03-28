package org.jboss.windup.rules.apps.java.reporting.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.Query;
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
 */
public class CreateJarDependencyReportRuleProvider extends AbstractRuleProvider
{
    public static final String TEMPLATE = "/reports/templates/jar_report.ftl";
    public static final String REPORT_DESCRIPTION = "This report displays all JAR dependencies found within the application.";

    public CreateJarDependencyReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(CreateJarDependencyReportRuleProvider.class, "Create JAR Dependency Report")
                    .setPhase(ReportGenerationPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(ArchiveModel.class))
                    .perform(new GraphOperation()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context)
                        {
                            // configuration of current execution
                            WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                            createGlobalReport(event.getGraphContext(), configurationModel);

                            for (FileModel inputPath : configurationModel.getInputPaths())
                            {
                                ProjectModel projectModel = inputPath.getProjectModel();
                                createReport(event.getGraphContext(), projectModel);
                            }
                        }

                        @Override
                        public String toString()
                        {
                            return "CreateRemoteServiceReport";
                        }
                    });

    }

    private void addAll(Set<String> sha1DupeCheck, List<ArchiveModel> projects, ProjectModel project)
    {
        FileModel rootFileModel = project.getRootFileModel();
        if (rootFileModel instanceof ArchiveModel && !sha1DupeCheck.contains(rootFileModel.getSHA1Hash()))
        {
            sha1DupeCheck.add(rootFileModel.getSHA1Hash());
            projects.add((ArchiveModel) rootFileModel);
        }

        for (ProjectModel child : project.getChildProjects())
            addAll(sha1DupeCheck, projects, child);
    }

    private void createGlobalReport(GraphContext context, WindupConfigurationModel configuration)
    {
        Set<String> sha1DupeCheck = new HashSet<>();
        List<ArchiveModel> dependencies = new ArrayList<>();
        for (FileModel inputApplication : configuration.getInputPaths())
            addAll(sha1DupeCheck, dependencies, inputApplication.getProjectModel());

        ReportService reportService = new ReportService(context);
        ApplicationReportModel reportModel = createReportModel(context, dependencies);
        reportModel.setDisplayInGlobalApplicationIndex(true);
        reportService.setUniqueFilename(reportModel, "dependency_report", "html");
    }

    private void createReport(GraphContext context, ProjectModel application)
    {
        Set<String> sha1DupeCheck = new HashSet<>();
        List<ArchiveModel> dependencies = new ArrayList<>();
        addAll(sha1DupeCheck, dependencies, application);

        if (dependencies.isEmpty())
            return;

        ReportService reportService = new ReportService(context);
        ApplicationReportModel reportModel = createReportModel(context, dependencies);
        reportModel.setProjectModel(application);
        reportService.setUniqueFilename(reportModel, "dependency_report_" + application.getName(), "html");
    }

    private ApplicationReportModel createReportModel(GraphContext context, List<ArchiveModel> dependencies)
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
