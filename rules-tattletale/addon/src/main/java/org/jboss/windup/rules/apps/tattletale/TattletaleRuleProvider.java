package org.jboss.windup.rules.apps.tattletale;

import java.nio.file.Paths;

import org.jboss.tattletale.Main;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class TattletaleRuleProvider extends WindupRuleProvider
{
    private static final String TATTLETALE_REPORT_SUBDIR = "tattletale";

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new TattletaleOperation());
    }

    private class TattletaleOperation extends GraphOperation
    {

        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
            FileModel inputFM = cfg.getInputPath();
            String inputPath = inputFM.getFilePath();
            String reportDirectory = new ReportService(event.getGraphContext()).getReportDirectory();
            String tattletaleDir = Paths.get(reportDirectory, TATTLETALE_REPORT_SUBDIR).toString();

            Main main = new Main();
            main.setSource(inputPath);
            main.setDestination(tattletaleDir);
            try
            {
                main.execute();
            }
            catch (Exception e)
            {
                throw new WindupException("Failed to run Tattletale due to: " + e.getMessage());
            }
        }
    }
}
