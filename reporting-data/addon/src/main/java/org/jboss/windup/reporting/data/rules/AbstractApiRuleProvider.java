package org.jboss.windup.reporting.data.rules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
    public static final String JAVASCRIPT_OUTPUT = "windup.js";

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        performProcess(event);
                    }
                });
    }
    // @formatter:on

    public abstract String getOutputFilename();

    public abstract Object getData(GraphRewrite event);

    private void performProcess(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        Object object = getData(event);

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
        File outputJSONFile = outputDir.resolve(getOutputFilename()).toFile();
        File outputJSFile = outputDir.resolve(JAVASCRIPT_OUTPUT).toFile();

        try {
            FileUtils.forceMkdir(outputJSONFile.getParentFile());
        } catch (IOException ex) {
            LOG.error("Error creating a directory: " + outputJSONFile.getParentFile().getPath());
            return;
        }

        // Create JSON file
        try (FileWriter writer = new FileWriter(outputJSONFile)) {
            writer.append(json);
            LOG.info("Exporting json data to file: " + outputJSONFile.getPath());
        } catch (IOException e) {
            LOG.error("Error exporting tags data to: " + outputJSONFile.getPath());
            return;
        }

        // Enrich Javascript file
        try (FileWriter writer = new FileWriter(outputJSFile, true)) {
            writer.append("window." + FilenameUtils.removeExtension(outputJSONFile.getName()) + "=" + json + ";");
            LOG.info("Exporting json data to file: " + outputJSFile.getPath());
        } catch (IOException e) {
            LOG.error("Error exporting tags data to: " + outputJSFile.getPath());
            return;
        }
    }

}
