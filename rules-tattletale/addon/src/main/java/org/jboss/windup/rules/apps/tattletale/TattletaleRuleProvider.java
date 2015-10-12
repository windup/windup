package org.jboss.windup.rules.apps.tattletale;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;

import org.jboss.tattletale.Main;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
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


/**
 * Runs Tattletale on the Windup's input.
 */
public class TattletaleRuleProvider extends AbstractRuleProvider
{
    private static final String TATTLETALE_REPORT_SUBDIR = "tattletale";

    public TattletaleRuleProvider()
    {
        super(MetadataBuilder.forProvider(TattletaleRuleProvider.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new TattletaleOperation());
    }

    private class TattletaleOperation extends GraphOperation
    {
        private static final String TTALE_CONFIG_FILE_NAME = "tattletale-config.properties";

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
                // The only way Tattletale accepts configuration is through a file.
                new File(tattletaleDir).mkdirs();
                File configPath = new File(tattletaleDir, TTALE_CONFIG_FILE_NAME);
                PrintStream str = new PrintStream(configPath);
                str.append("enableDot=false\n"); // Whether to generate .dot and .png
                str.append("graphvizDot=dot\n"); // Dot executable
                str.close();
                main.setConfiguration(configPath.getAbsolutePath());

                main.execute();
            }
            catch (Exception e)
            {
                throw new WindupException("Failed to run Tattletale due to: " + e.getMessage());
            }
        }
    }
}
