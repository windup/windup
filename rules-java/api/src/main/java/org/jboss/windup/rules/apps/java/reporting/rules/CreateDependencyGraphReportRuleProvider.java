package org.jboss.windup.rules.apps.java.reporting.rules;

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
import org.jboss.windup.rules.apps.java.condition.SourceMode;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.List;

/**
 * Generates the dependency graph reports for the applications analyzed
 */
@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateDependencyGraphReportRuleProvider extends AbstractRuleProvider
{
   public static final String REPORT_NAME = "Dependencies Graph";
   public static final String SINGLE_APPLICATION_REPORT_DESCRIPTION = "This graph shows the dependencies embedded within the analyzed application";
   public static final String GLOBAL_REPORT_DESCRIPTION = SINGLE_APPLICATION_REPORT_DESCRIPTION + "s";
   public static final String TEMPLATE = "/reports/templates/dependency_graph.ftl";
   private static final String REPORT_BASEFILENAME = "dependency_graph_report";

   @Override
   public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
   {
      return ConfigurationBuilder.begin()
               .addRule()
               .perform(new CreateDependencyGraphReportOperation());
   }

   private class CreateDependencyGraphReportOperation extends GraphOperation
   {

      @Override
      public void perform(GraphRewrite event, EvaluationContext context)
      {
         List<FileModel> inputPaths = WindupConfigurationService.getConfigurationModel(event.getGraphContext())
                  .getInputPaths();
         if (inputPaths.size() > 1)
         {
            createGlobalAppDependencyGraphReport(event.getGraphContext());
         }
         inputPaths.stream()
                  .filter(inputPath -> !ProjectModel.TYPE_VIRTUAL.equals(inputPath.getProjectModel().getProjectType()))
                  .forEach(inputPath -> createSingleAppDependencyGraphReport(event.getGraphContext(),
                           inputPath.getProjectModel()));
      }

      private ApplicationReportModel createAppDependencyGraphReport(GraphContext context)
      {
         ApplicationReportService applicationReportService = new ApplicationReportService(context);
         ApplicationReportModel report = applicationReportService.create();
         report.setReportPriority(104);
         report.setReportIconClass("glyphicon glyphicon-tree-deciduous");
         report.setTemplatePath(TEMPLATE);
         report.setTemplateType(TemplateType.FREEMARKER);
         report.setDisplayInApplicationReportIndex(Boolean.TRUE);
         return report;
      }

      private void createSingleAppDependencyGraphReport(GraphContext context, ProjectModel projectModel)
      {
         ReportService reportService = new ReportService(context);
         ApplicationReportModel report = createAppDependencyGraphReport(context);
         report.setReportName(REPORT_NAME);
         report.setProjectModel(projectModel);
         report.setMainApplicationReport(Boolean.FALSE);
         report.setDescription(SINGLE_APPLICATION_REPORT_DESCRIPTION + ".");
         reportService.setUniqueFilename(report, REPORT_BASEFILENAME, "html");
      }

      private void createGlobalAppDependencyGraphReport(GraphContext context)
      {
         ReportService reportService = new ReportService(context);
         ApplicationReportModel report = createAppDependencyGraphReport(context);
         report.setReportName(REPORT_NAME);
         report.setDisplayInGlobalApplicationIndex(Boolean.TRUE);
         report.setDescription(GLOBAL_REPORT_DESCRIPTION + ".");
         reportService.setUniqueFilename(report, REPORT_BASEFILENAME + "_global", "html");
      }
   }
}
