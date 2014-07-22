package org.jboss.windup.reporting.freemarker;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreeMarkerOperation extends GraphOperation
{
    private static final Logger LOG = LoggerFactory.getLogger(FreeMarkerOperation.class);

    private String templatePath;
    private String outputFilename;
    private String reportName;
    private ReportModel parentReportModel;
    private List<String> variableNames = new ArrayList<>();

    public FreeMarkerOperation(String templatePath, String outputFilename, String... varNames)
    {
        this.templatePath = templatePath;
        this.outputFilename = outputFilename;
        this.variableNames = Arrays.asList(varNames);
    }

    public static FreeMarkerOperation create(String templatePath, String outputFilename, String... varNames)
    {
        return new FreeMarkerOperation(templatePath, outputFilename, varNames);
    }

    public FreeMarkerOperation reportName(String reportName)
    {
        this.reportName = reportName;
        return this;
    }

    public FreeMarkerOperation parentReport(ReportModel parentReport)
    {
        this.parentReportModel = parentReport;
        return this;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        try
        {
            WindupConfigurationModel windupCfg = GraphService.getConfigurationModel(event.getGraphContext());
            String outputDir = windupCfg.getOutputPath().getFilePath();
            Path outputPath = Paths.get(outputDir, outputFilename);

            LOG.info("Reporting: Writing template \"{}\" to output file \"{}\"", templatePath, outputPath
                        .toAbsolutePath().toString());

            freemarker.template.Configuration cfg = new freemarker.template.Configuration();
            cfg.setTemplateLoader(new FurnaceFreeMarkerTemplateLoader());
            cfg.setTemplateUpdateDelay(500);
            Template template = cfg.getTemplate(templatePath);

            Variables varStack = Variables.instance(event);
            Map<String, Object> objects = findAllVariablesAsMap(varStack,
                        variableNames.toArray(new String[variableNames
                                    .size()]));

            try (FileWriter fw = new FileWriter(outputPath.toFile()))
            {
                template.process(objects, fw);
            }

            ReportModel reportModel = event.getGraphContext().getFramed().addVertex(null, ReportModel.class);
            reportModel.setTemplatePath(templatePath);
            reportModel.setTemplateType(TemplateType.FREEMARKER);

            if (parentReportModel != null)
            {
                reportModel.setParentReport(parentReportModel);
            }
            if (reportName != null)
            {
                reportModel.setReportName(reportName);
            }

            addAssociatedReportData(event.getGraphContext(), reportModel, objects);
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to write template results due to: " + e.getMessage(), e);
        }
        catch (TemplateException e)
        {
            throw new WindupException("FreeMarkerOperation TemplateException: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void addAssociatedReportData(GraphContext context, ReportModel reportModel, Map<String, Object> reportData)
    {
        Map<String, WindupVertexFrame> relatedResources = new HashMap<>();
        for (Map.Entry<String, Object> varEntry : reportData.entrySet())
        {
            Object value = varEntry.getValue();
            if (value instanceof WindupVertexFrame)
            {
                relatedResources.put(varEntry.getKey(), (WindupVertexFrame) varEntry.getValue());
            }
            else if (value instanceof Iterable)
            {
                WindupVertexListModel list = context.getFramed().addVertex(null, WindupVertexListModel.class);
                for (WindupVertexFrame frame : (Iterable<? extends WindupVertexFrame>) value)
                {
                    list.addItem(frame);
                }
                relatedResources.put(varEntry.getKey(), list);
            }
            else
            {
                throw new WindupException("Unrecognized variable type: " + value.getClass().getCanonicalName()
                            + " encountered!");
            }
        }
    }

    /**
     * Searches the variables layers, top to bottom, for the given variable names and returns them in a map of "name" ->
     * {@link Iterable}<?> pairs.
     */
    static Map<String, Object> findAllVariablesAsMap(Variables varStack, String... varNames)
    {
        Map<String, Object> results = new HashMap<String, Object>();
        for (String varName : varNames)
        {
            WindupVertexFrame payload = null;
            try
            {
                payload = Iteration.getCurrentPayload(varStack, null, varName);
            }
            catch (IllegalStateException | IllegalArgumentException e)
            {
                // oh well
            }

            if (payload != null)
            {
                results.put(varName, payload);
            }
            else
            {
                Iterable<WindupVertexFrame> var = varStack.findVariable(varName);
                if (var != null)
                {
                    results.put(varName, var);
                }
            }
        }
        return results;
    }
}
