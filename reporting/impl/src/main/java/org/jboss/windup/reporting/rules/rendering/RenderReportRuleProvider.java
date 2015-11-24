package org.jboss.windup.reporting.rules.rendering;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.reporting.freemarker.FreeMarkerIterationOperation;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.threading.WindupExecutors;
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
    private static final Logger LOG = Logging.get(RenderReportRuleProvider.class);

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
        return ConfigurationBuilder
            .begin()
            .addRule()
            .when(Query.fromType(ReportModel.class).withProperty(ReportModel.TEMPLATE_TYPE, TemplateType.FREEMARKER.toString()))
            .perform(new FreeMarkerThreadedRenderer(furnace))
            
            .addRule()
            .when(Query.fromType(ReportResourceFileModel.class))
            .perform(new AbstractIterationOperation<ReportResourceFileModel>() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, ReportResourceFileModel payload) {
                    ReportService reportService = new ReportService(event.getGraphContext());
                    Path outputDir = Paths.get(reportService.getReportDirectory());

                    File directory = outputDir.toFile();
                    File fullPath = new File(directory, FilenameUtils.separatorsToSystem("resources/" + payload.getPrettyPath()));

                    try {
                        FileUtils.forceMkdir(fullPath.getParentFile());
                        FileUtils.copyFile(payload.asFile(), fullPath);
                        LOG.info("Copied raw file: " + payload.getFilePath() + " to: " + fullPath.getAbsolutePath());
                    } catch (IOException e) {
                        LOG.warning("Exception creating file: " + fullPath.getAbsolutePath());
                    }
                }
            });
    }
    // @formatter:on

    private class FreeMarkerThreadedRenderer extends GraphOperation
    {
        private final Furnace furnace;

        public FreeMarkerThreadedRenderer(Furnace furnace)
        {
            this.furnace = furnace;
        }

        @Override
        public void perform(final GraphRewrite event, final EvaluationContext context)
        {
            Iterable<? extends WindupVertexFrame> reportModelsIterable = Variables.instance(event)
                        .findVariable(Iteration.DEFAULT_VARIABLE_LIST_STRING);
            final Queue<WindupVertexFrame> reportModels = new ConcurrentLinkedDeque<>();
            for (WindupVertexFrame frame : reportModelsIterable)
                reportModels.add(frame);
            final FreeMarkerIterationOperation freeMarkerIterationOperation = FreeMarkerIterationOperation.create(furnace);
            final IterationProgress iterationProgress = IterationProgress.monitoring("Rendering Reports", 100);

            int threadCount = WindupExecutors.getDefaultThreadCount();
            ExecutorService executorService = WindupExecutors.newFixedThreadPool(WindupExecutors.getDefaultThreadCount());

            // Set the frames as iteration progress uses this
            event.getRewriteContext().put(Iteration.DEFAULT_VARIABLE_LIST_STRING, reportModels);
            iterationProgress.perform(event, context);

            for (int i = 0; i < threadCount; i++)
            {
                executorService.submit(new Callable<Void>()
                {
                    @Override
                    public Void call() throws Exception
                    {
                        while (true)
                        {
                            final ReportModel reportModel;
                            WindupVertexFrame reportModelObject = reportModels.remove();
                            if (reportModelObject == null)
                                return null;
                            reportModel = (ReportModel) reportModelObject;

                            try
                            {
                                Thread.currentThread().setName(reportModel.getTemplatePath() + "_" + reportModel.getReportFilename());

                                iterationProgress.perform(event, context);
                                freeMarkerIterationOperation.perform(event, context, reportModel);
                            }
                            catch (Throwable t)
                            {
                                LOG.log(Level.WARNING, "Failed to render freemarker report: " + reportModel + " due to: " + t.getMessage(), t);
                            }
                        }
                    }
                });
            }
            executorService.shutdown();
            try
            {
                executorService.awaitTermination(2, TimeUnit.DAYS);
            }
            catch (InterruptedException e)
            {
                throw new WindupException("Failed to render reports due to a timeout: " + e.getMessage(), e);
            }

            // reset them
            event.getRewriteContext().put(Iteration.DEFAULT_VARIABLE_LIST_STRING, null);
        }
    }
}
