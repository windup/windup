package org.jboss.windup.reporting.rules.generation;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateFreeMarkerMethodReportRuleProvider extends AbstractRuleProvider {
    private static final String TEMPLATE = "/reports/templates/windupfreemarkerfunctions.ftl";
    private static final String REPORT_NAME = "Windup FreeMarker Function Report";
    private static final String OUTPUT_FILENAME = "windup_freemarkerfunctions.html";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new CreateMethodReport())
                .withId("CreateWindupFreeMarkerFunctionReport");
    }

    private class CreateMethodReport extends GraphOperation {

        @Override
        public void perform(GraphRewrite event, EvaluationContext context) {
            ReportService reportService = new ReportService(event.getGraphContext());
            ReportModel reportModel = reportService.create();
            reportModel.setReportFilename(OUTPUT_FILENAME);
            reportModel.setReportName(REPORT_NAME);
            reportModel.setTemplateType(TemplateType.FREEMARKER);
            reportModel.setTemplatePath(TEMPLATE);
        }
    }
}
