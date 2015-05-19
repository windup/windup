package org.jboss.windup.reporting.rules.rendering;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationFilter;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.reporting.freemarker.FreeMarkerIterationOperation;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.rules.generation.CreateSourceReportRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This renders the ApplicationReport, along with all of its subapplications via freemarker.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class RenderReportRuleProvider extends AbstractRuleProvider
{
    private static Logger LOG = Logging.get(RenderReportRuleProvider.class);
    
    @Inject
    private Furnace furnace;

    public RenderReportRuleProvider()
    {
        super(MetadataBuilder.forProvider(RenderReportRuleProvider.class)
                    .setPhase(ReportRenderingPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        FreeMarkerIterationOperation reportOperation = FreeMarkerIterationOperation.create(furnace);

        return ConfigurationBuilder
            .begin()
            .addRule()
            .when(Query.fromType(ReportModel.class))
            .perform(
                        Iteration.over()
                            .when(new AbstractIterationFilter<ReportModel>()
                            {
                                @Override
                                public boolean evaluate(GraphRewrite event, EvaluationContext context, ReportModel payload)
                                {
                                    return TemplateType.FREEMARKER.equals(payload.getTemplateType());
                                }
                                
                                @Override
                                public String toString()
                                {
                                    return "ReportModel.templateType == TemplateType.FREEMARKER";
                                }
                            })
                            .perform(reportOperation)
                            .endIteration()
            )
            
            .addRule()
            .when(Query.fromType(ReportResourceFileModel.class))
            .perform(new AbstractIterationOperation<ReportResourceFileModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, ReportResourceFileModel payload)
                {
                    ReportService reportService = new ReportService(event.getGraphContext());
                    Path outputDir = Paths.get(reportService.getReportDirectory());
                    
                    File directory = outputDir.toFile();
                    File fullPath = new File(directory, FilenameUtils.separatorsToSystem("resources/"+payload.getPrettyPath()));
                    
                    try
                    {
                        FileUtils.forceMkdir(fullPath.getParentFile());
                        FileUtils.copyFile(payload.asFile(), fullPath);
                        LOG.info("Copied raw file: "+payload.getFilePath()+" to: "+fullPath.getAbsolutePath());
                    }
                    catch (IOException e)
                    {
                        LOG.warning("Exception creating file: "+fullPath.getAbsolutePath());
                    }
                }
            })
            ;
    }
    // @formatter:on
}
