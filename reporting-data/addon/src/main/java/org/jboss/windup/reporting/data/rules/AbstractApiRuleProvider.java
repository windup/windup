package org.jboss.windup.reporting.data.rules;

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
import org.jboss.windup.util.threading.WindupExecutors;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public abstract class AbstractApiRuleProvider extends AbstractRuleProvider {

    private static final Logger LOG = Logger.getLogger(AbstractApiRuleProvider.class);

    public static final String JAVASCRIPT_OUTPUT = "windup.js";

    public static final Map<GraphContext, ExecutorService> executorServiceMap = new ConcurrentHashMap<>();

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {

        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        GraphContext graphContext = event.getGraphContext();

                        executorServiceMap.putIfAbsent(graphContext, WindupExecutors.newFixedThreadPool(WindupExecutors.getDefaultThreadCount()));
                        ExecutorService executorService = executorServiceMap.get(graphContext);
                        executorService.submit(() -> performProcess(event));
                    }
                });
    }
    // @formatter:on

    public abstract String getBasePath();

    public abstract Object getAll(GraphRewrite event);

    public abstract Map<String, Object> getById(GraphRewrite event);

    protected void performProcess(GraphRewrite event) {
        GraphContext context = event.getGraphContext();

        ReportService reportService = new ReportService(context);
        Path outputBaseDir = reportService.getWindupUIApiDirectory();

        // Create getAll Json file
        File getAllJsonFile = outputBaseDir.resolve(getBasePath() + ".json").toFile();
        try {
            FileUtils.forceMkdir(getAllJsonFile.getParentFile());
        } catch (IOException ex) {
            LOG.error("Error creating directory: " + getAllJsonFile.getParentFile().getPath());
            return;
        }

        ObjectWriter ow = new ObjectMapper().writer();
        String getAllJsonString;

        Object getAll = getAll(event);
        try (FileWriter writer = new FileWriter(getAllJsonFile)) {
            getAllJsonString = ow.writeValueAsString(getAll);
            writer.append(getAllJsonString);
            LOG.info("Exporting json data to file: " + getAllJsonFile.getPath());
        } catch (JsonProcessingException e) {
            LOG.error("Error mapping object to JSON");
            return;
        } catch (IOException e) {
            LOG.error("Error exporting data to: " + getAllJsonFile.getPath());
            return;
        }

        // Create byId Json files
        Map<String, Object> getById = getById(event);
        for (Map.Entry<String, Object> entry : getById.entrySet()) {
            File byIdJsonFile = outputBaseDir.resolve(getBasePath()).resolve(entry.getKey() + ".json").toFile();

            try {
                FileUtils.forceMkdir(byIdJsonFile.getParentFile());
            } catch (IOException ex) {
                LOG.error("Error creating directory: " + getAllJsonFile.getParentFile().getPath());
                return;
            }

            try (FileWriter writer = new FileWriter(byIdJsonFile)) {
                String byIdJsonString = ow.writeValueAsString(entry.getValue());
                writer.append(byIdJsonString);
            } catch (JsonProcessingException e) {
                LOG.error("Error mapping object to JSON");
                return;
            } catch (IOException e) {
                LOG.error("Error exporting data to: " + byIdJsonFile.getPath());
                return;
            }
        }

        String getByIdJsonString;
        try {
            getByIdJsonString = ow.writeValueAsString(getById);
        } catch (JsonProcessingException e) {
            LOG.error("Error mapping object to JSON");
            return;
        }

        // Enrich Javascript file
        enrichWindupJS(outputBaseDir, getBasePath(), getAllJsonString, getByIdJsonString);
    }

    private static synchronized void enrichWindupJS(Path outputBaseDir, String basePath, String getAllJsonString, String getByIdJsonString) {
        File outputJSFile = outputBaseDir.resolve(JAVASCRIPT_OUTPUT).toFile();
        try (
                FileWriter fileWriter = new FileWriter(outputJSFile, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        ) {
            String all = "window[\"" + basePath + "\"]=" + getAllJsonString + ";";
            String byId = "window[\"" + basePath + "_by_id\"]=" + getByIdJsonString + ";";

            bufferedWriter.write(all + byId);
            bufferedWriter.flush();
            LOG.info("Exporting json data to file: " + outputJSFile.getPath());
        } catch (IOException e) {
            LOG.error("Error exporting data to: " + outputJSFile.getPath());
        }
    }

}
