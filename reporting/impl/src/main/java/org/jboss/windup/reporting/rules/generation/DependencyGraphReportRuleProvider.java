package org.jboss.windup.reporting.rules.generation;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.google.common.collect.Iterables;

@RuleMetadata(phase = ReportGenerationPhase.class)
public class DependencyGraphReportRuleProvider extends AbstractRuleProvider
{
    private static final String TEMPLATE_PATH = "/reports/templates/application-libraries.ftl";
    private static final String REPORT_DESCRIPTION = "TODO";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new CreateDependencyGraphReportOperation());
    }

    private class CreateDependencyGraphReportOperation extends GraphOperation
    {
        private static final String DEPENDENCIES_GRAPH_REPORT_NAME = "Dependencies Graph";

        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            int inputApplicationCount = Iterables.size(WindupConfigurationService.getConfigurationModel(event.getGraphContext()).getInputPaths());
            if (inputApplicationCount > 1) {
                createGlobalAppDependencyGraphReport(event.getGraphContext());
            }

            for (FileModel inputPath : WindupConfigurationService.getConfigurationModel(event.getGraphContext()).getInputPaths()) {
                ApplicationReportModel report = createSingleAppDependencyGraphReport(event.getGraphContext(), inputPath.getProjectModel());
                report.setMainApplicationReport(Boolean.FALSE);
            }
        }

        private ApplicationReportModel createAppDependencyGraphReport(GraphContext context)
        {
            ApplicationReportService applicationReportService = new ApplicationReportService(context);
            ApplicationReportModel report = applicationReportService.create();
            report.setReportPriority(104);
            report.setReportIconClass("glyphicon glyphicon-tree-deciduous");
            report.setTemplatePath(TEMPLATE_PATH);
            report.setTemplateType(TemplateType.FREEMARKER);
            report.setDisplayInApplicationReportIndex(Boolean.TRUE);
            report.setDescription(REPORT_DESCRIPTION);
            return report;
        }

        private ApplicationReportModel createSingleAppDependencyGraphReport(GraphContext context, ProjectModel projectModel)
        {
            ReportService reportService = new ReportService(context);
            ApplicationReportModel report = createAppDependencyGraphReport(context);
            report.setReportName(DEPENDENCIES_GRAPH_REPORT_NAME);
            report.setProjectModel(projectModel);
            reportService.setUniqueFilename(report, "application_graph", "html");
            return report;
        }

        private ApplicationReportModel createGlobalAppDependencyGraphReport(GraphContext context)
        {
            ReportService reportService = new ReportService(context);
            ApplicationReportModel report = createAppDependencyGraphReport(context);
            report.setReportName(DEPENDENCIES_GRAPH_REPORT_NAME);
            report.setDisplayInGlobalApplicationIndex(Boolean.TRUE);
            reportService.setUniqueFilename(report, "application_graph", "html");
            return report;
        }
    }
}
