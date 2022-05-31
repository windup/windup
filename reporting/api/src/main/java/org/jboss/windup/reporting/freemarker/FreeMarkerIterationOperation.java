package org.jboss.windup.reporting.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.lock.LockMode;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.ExecutionStatistics;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to produce a freemarker report from inside of a Windup {@link Iteration}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a> <jesse.sightler@gmail.com)
 */
public class FreeMarkerIterationOperation extends AbstractIterationOperation<ReportModel> {
    private static final Logger LOG = Logger.getLogger(FreeMarkerIterationOperation.class.getName());
    private static final String DEFAULT_ITERATION_PAYLOAD_NAME = "reportModel";
    private static final String FM_VAR_EVENT = "event";
    private static final String FM_VAR_WINDUP_CONFIG = "windupConfig";
    private static final String FM_VAR_INPUT_PATHS = "inputPaths";
    private static final String FM_VAR_APPS = "inputApplications";

    private final Furnace furnace;
    private final Set<String> variableNames = new HashSet<>();
    private final boolean useDefaultPayloadVariableName;

    protected FreeMarkerIterationOperation(Furnace furnace, String... varNames) {
        super();
        this.furnace = furnace;
        useDefaultPayloadVariableName = true;
        if (varNames != null) {
            variableNames.addAll(Arrays.asList(varNames));
        }
    }

    protected FreeMarkerIterationOperation(Furnace furnace, String iterationVarName, String... varNames) {
        super(iterationVarName);
        this.furnace = furnace;
        useDefaultPayloadVariableName = false;
        variableNames.add(iterationVarName);
        if (varNames != null) {
            variableNames.addAll(Arrays.asList(varNames));
        }
    }

    /**
     * Create a FreeMarkerIterationOperation with the provided furnace instance, the provided iteration var, as well as any other associated variables
     * (based upon variables in the Variables object).
     */
    public static FreeMarkerIterationOperation create(Furnace furnace, String... varNames) {
        return new FreeMarkerIterationOperation(furnace, varNames);
    }

    @Override
    public void perform(final GraphRewrite event, final EvaluationContext evalCtx, final ReportModel payload) {
        String templatePath = payload.getTemplatePath().replace('\\', '/');
        String outputFilename = payload.getReportFilename();

        ExecutionStatistics.get().begin("FreeMarkerIterationOperation.render(" + templatePath + ", " + outputFilename + ")");
        try {
            final GraphContext grCtx = event.getGraphContext();
            ReportService reportService = new ReportService(grCtx);

            Path outputPath = reportService.getReportDirectory().resolve(outputFilename);
            Files.createDirectories(outputPath.getParent());

            LOG.info("Reporting: Writing template \"" + templatePath + "\" to output file \"" + outputPath.toAbsolutePath().toString() + "\"");

            Configuration freemarkerConfig = FreeMarkerUtil.getDefaultFreemarkerConfiguration();

            Template template = freemarkerConfig.getTemplate(templatePath);

            Variables windupVarStack = Variables.instance(event);

            // just the variables
            Map<String, Object> freeMarkerTemplateVariables = FreeMarkerUtil.findFreeMarkerContextVariables(
                    windupVarStack, variableNames.toArray(new String[variableNames.size()]));

            if (useDefaultPayloadVariableName) {
                freeMarkerTemplateVariables.put(DEFAULT_ITERATION_PAYLOAD_NAME, payload);
            }

            // Additional objects to be available to the template.
            freeMarkerTemplateVariables.put(FM_VAR_EVENT, event);
            WindupConfigurationModel windupConfigModel = WindupConfigurationService.getConfigurationModel(grCtx);
            freeMarkerTemplateVariables.put(FM_VAR_WINDUP_CONFIG, windupConfigModel);
            freeMarkerTemplateVariables.put(FM_VAR_INPUT_PATHS, windupConfigModel.getInputPaths());
            freeMarkerTemplateVariables.put(FM_VAR_APPS, new ProjectService(grCtx).getRootProjectModels());

            // Also, extension functions (these are kept separate from freeMarkerTemplateVariables in order to prevent them
            // from being stored in the associated data with the reportmodel)
            final Map<String, Object> freeMarkerExtensions;
            freeMarkerExtensions = furnace.getLockManager().performLocked(LockMode.WRITE, new Callable<Map<String, Object>>() {
                @Override
                public Map<String, Object> call() throws Exception {
                    return FreeMarkerUtil.findFreeMarkerExtensions(furnace, event);
                }
            });

            Map<String, Object> objects = new HashMap<>(freeMarkerTemplateVariables);
            objects.putAll(freeMarkerExtensions);

            try (FileWriter fw = new FileWriter(outputPath.toFile())) {
                template.process(objects, fw);
            }
        } catch (IOException | TemplateException e) {
            LOG.log(Level.WARNING,
                    System.lineSeparator() + "   Failed to write template: " + templatePath
                            + System.lineSeparator() + "   To: " + outputFilename
                            + System.lineSeparator() + "   Due to: " + e.getMessage(), e);
        } finally {
            ExecutionStatistics.get().end("FreeMarkerIterationOperation.render(" + templatePath + ", " + outputFilename + ")");
        }
    }

    @Override
    public String toString() {
        return "RenderFreeMarkerTemplate";
    }
}
