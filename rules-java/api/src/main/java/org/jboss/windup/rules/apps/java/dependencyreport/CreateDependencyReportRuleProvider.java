package org.jboss.windup.rules.apps.java.dependencyreport;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.TemplateType;
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
@RuleMetadata(phase = ReportGenerationPhase.class, id = "Create Java Dependency Report")
public class CreateDependencyReportRuleProvider extends AbstractRuleProvider
{
    public static final String REPORT_NAME = "Dependencies";
    public static final String TEMPLATE = "/reports/templates/dependency_report.ftl";
    public static final String REPORT_DESCRIPTION = "This report lists all found Java libraries embedded within the analyzed application.";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
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
                return "CreateDependencyReport";
            }
        });
        // @formatter:on
    }

    private void addAll(GraphContext context,
                DependenciesReportModel reportModel,
                ProjectModelTraversal traversal,
                Map<String, DependencyReportDependencyGroupModel> groupsBySHA1)
    {
        FileModel rootFileModel = traversal.getCurrent().getRootFileModel();

        // Don't create a dependency entry for the entire application (root project)
        boolean isRootProject = traversal.getCurrent().getParentProject() == null;
        if (!isRootProject && rootFileModel instanceof ArchiveModel)
        {
            ArchiveModel archiveModel = (ArchiveModel) rootFileModel;
            ArchiveModel canonicalArchive;
            if (archiveModel instanceof DuplicateArchiveModel)
                canonicalArchive = ((DuplicateArchiveModel) archiveModel).getCanonicalArchive();
            else
                canonicalArchive = archiveModel;

            // 1. Get SHA1
            String sha1 = archiveModel.getSHA1Hash();

            // 2. Get the group model for this sha1
            DependencyReportDependencyGroupModel groupModel = groupsBySHA1.get(sha1);
            if (groupModel == null)
            {
                groupModel = context.service(DependencyReportDependencyGroupModel.class).create();
                groupModel.setSHA1(sha1);
                groupModel.setCanonicalProject(canonicalArchive.getProjectModel());
                reportModel.addArchiveGroup(groupModel);

                groupsBySHA1.put(sha1, groupModel);
            }

            // 3. If the group already has this archive, don't do anything
            String path = traversal.getFilePath(rootFileModel);
            boolean archiveAlreadyLinked = false;
            for (DependencyReportToArchiveEdgeModel groupEdge : groupModel.getArchives())
            {
                if (StringUtils.equals(groupEdge.getFullPath(), path))
                {
                    archiveAlreadyLinked = true;
                    break;
                }
            }

            // Don't add projects that have already been added
            if (!archiveAlreadyLinked)
            {
                DependencyReportToArchiveEdgeModel edge = groupModel.addArchiveModel(archiveModel);
                edge.setFullPath(path);
            }
        }

        for (ProjectModelTraversal child : traversal.getChildren())
            addAll(context, reportModel, child, groupsBySHA1);
    }

    private void createGlobalReport(GraphContext context, WindupConfigurationModel configuration)
    {
        ReportService reportService = new ReportService(context);
        DependenciesReportModel reportModel = createReportModel(context);

        Map<String, DependencyReportDependencyGroupModel> sha1ToGroup = new HashMap<>();
        for (FileModel inputApplication : configuration.getInputPaths())
        {
            ProjectModel projectModel = inputApplication.getProjectModel();

            // Do not include shared libs in the global report as this is not really a user app
            if (StringUtils.equals(projectModel.getUniqueID(), ProjectService.SHARED_LIBS_UNIQUE_ID))
                continue;

            ProjectModelTraversal traversal = new ProjectModelTraversal(inputApplication.getProjectModel());
            addAll(context, reportModel, traversal, sha1ToGroup);
        }

        reportModel.setDisplayInGlobalApplicationIndex(Boolean.TRUE);
        reportService.setUniqueFilename(reportModel, "dependency_report_global", "html");
    }

    private void createReport(GraphContext context, ProjectModel application)
    {
        ReportService reportService = new ReportService(context);
        DependenciesReportModel reportModel = createReportModel(context);

        addAll(context, reportModel, new ProjectModelTraversal(application), new HashMap<String, DependencyReportDependencyGroupModel>());

        reportModel.setProjectModel(application);
        reportService.setUniqueFilename(reportModel, "dependency_report_" + application.getName(), "html");
    }

    private DependenciesReportModel createReportModel(GraphContext context)
    {
        ApplicationReportService service = new ApplicationReportService(context);
        DependenciesReportModel applicationReportModel = context
                    .service(DependenciesReportModel.class)
                    .addTypeToModel(service.create());

        applicationReportModel.setDisplayInApplicationReportIndex(Boolean.TRUE);
        applicationReportModel.setReportPriority(120);
        applicationReportModel.setReportName(REPORT_NAME);
        applicationReportModel.setDescription(REPORT_DESCRIPTION);
        applicationReportModel.setReportIconClass("glyphicon glyphicon-retweet");
        applicationReportModel.setTemplatePath(TEMPLATE);
        applicationReportModel.setTemplateType(TemplateType.FREEMARKER);
        return applicationReportModel;

    }
}
