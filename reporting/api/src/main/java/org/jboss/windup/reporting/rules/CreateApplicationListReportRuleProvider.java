package org.jboss.windup.reporting.rules;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.rules.AttachApplicationReportsToIndexRuleProvider;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This renders an application index page listing all applications analyzed by the current execution of windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = PostReportGenerationPhase.class, before = AttachApplicationReportsToIndexRuleProvider.class)
public class CreateApplicationListReportRuleProvider extends AbstractRuleProvider
{
    public static final String APPLICATION_LIST_REPORT = "Application List";
    private static final String OUTPUT_FILENAME = "../index.html";
    public static final String TEMPLATE_PATH = "/reports/templates/application_list.ftl";

    @Inject
    private Furnace furnace;


    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .perform(new GraphOperation() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context) {
                    createIndexReport(event.getGraphContext());
                }
            });
    }
    // @formatter:on

    private void createIndexReport(GraphContext context)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);

        ApplicationReportModel report = applicationReportService.create();
        report.setReportPriority(1);
        report.setReportIconClass("glyphicon glyphicon-home");
        report.setReportName(APPLICATION_LIST_REPORT);
        report.setTemplatePath(TEMPLATE_PATH);
        report.setTemplateType(TemplateType.FREEMARKER);

        report.setDisplayInApplicationReportIndex(false);
        report.setReportFilename(OUTPUT_FILENAME);

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
        WindupVertexListModel<ApplicationReportModel> applications = listService.create();
        for (ApplicationReportModel applicationReportModel : applicationReportService.findAll())
        {
            if (applicationReportModel.isMainApplicationReport() != null && applicationReportModel.isMainApplicationReport())
                applications.addItem(applicationReportModel);
        }
        Map<String, WindupVertexFrame> relatedData = new HashMap<>();
        relatedData.put("applications", applications);

        report.setRelatedResource(relatedData);
    }
}
