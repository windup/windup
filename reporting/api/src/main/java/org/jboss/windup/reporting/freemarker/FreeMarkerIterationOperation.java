package org.jboss.windup.reporting.freemarker;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreeMarkerIterationOperation extends AbstractIterationOperation<ReportModel>
{
    private static final Logger LOG = LoggerFactory.getLogger(FreeMarkerIterationOperation.class);

    private final Furnace furnace;
    private final Set<String> variableNames = new HashSet<>();

    public FreeMarkerIterationOperation(Furnace furnace, String iterationVarName, String... varNames)
    {
        super(ReportModel.class, iterationVarName);
        this.furnace = furnace;
        variableNames.add(iterationVarName);
        if (varNames != null)
        {
            for (String varName : varNames)
            {
                variableNames.add(varName);
            }
        }
    }

    public static FreeMarkerIterationOperation create(Furnace furnace, String iterationVarName, String... varNames)
    {
        return new FreeMarkerIterationOperation(furnace, iterationVarName, varNames);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ReportModel payload)
    {
        try
        {
            String templatePath = payload.getTemplatePath();
            String outputFilename = payload.getReportFilename();

            WindupConfigurationModel windupCfg = GraphService.getConfigurationModel(event.getGraphContext());
            String outputDir = windupCfg.getOutputPath().getFilePath();
            Path outputPath = Paths.get(outputDir, outputFilename);

            LOG.info("Reporting: Writing template \"{}\" to output file \"{}\"", templatePath, outputPath
                        .toAbsolutePath().toString());

            Configuration freemarkerConfig = new Configuration();
            freemarkerConfig.setTemplateLoader(new FurnaceFreeMarkerTemplateLoader());
            freemarkerConfig.setTemplateUpdateDelay(500);

            Template template = freemarkerConfig.getTemplate(templatePath);

            Variables varStack = Variables.instance(event);

            // just the variables
            Map<String, Object> vars = FreeMarkerUtil.findFreeMarkerContextVariables(varStack,
                        variableNames.toArray(new String[variableNames
                                    .size()]));

            // also, extension functions
            Map<String, Object> freeMarkerExtensions = FreeMarkerUtil.findFreeMarkerExtensions(furnace);

            Map<String, Object> objects = new HashMap<>(vars);
            objects.putAll(freeMarkerExtensions);

            try (FileWriter fw = new FileWriter(outputPath.toFile()))
            {
                template.process(objects, fw);
            }

            FreeMarkerUtil.addAssociatedReportData(event.getGraphContext(), payload, vars);
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
}
