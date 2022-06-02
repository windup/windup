package org.jboss.windup.reporting.rules.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.service.ReportService;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;


public abstract class AbstractApiRuleProvider extends AbstractRuleProvider {

    private static final Logger LOG = Logger.getLogger(AbstractApiRuleProvider.class);

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        performProcess(event.getGraphContext());
                    }
                });
    }
    // @formatter:on

    public abstract String getOutputFilename();

    public abstract Object getData(GraphContext context);

    private void performProcess(GraphContext context) {
        Object object = getData(context);

        String json;
        try {
            ObjectWriter ow = new ObjectMapper().writer();
            json = ow.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error("Error mapping object to JSON");
            return;
        }

        ReportService reportService = new ReportService(context);
        Path outputDir = reportService.getApiDataDirectory();
        File outputFile = outputDir.resolve(getOutputFilename()).toFile();
        try {
            FileUtils.forceMkdir(outputFile.getParentFile());
        } catch (IOException ex) {
            LOG.error("Error creating a directory: " + outputFile.getParentFile().getPath());
            return;
        }

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.append(json);
            LOG.info("Exporting json data to file: " + outputFile.getPath());
        } catch (IOException e) {
            LOG.error("Error exporting tags data to: " + outputFile.getPath());
            return;
        }
    }

}
