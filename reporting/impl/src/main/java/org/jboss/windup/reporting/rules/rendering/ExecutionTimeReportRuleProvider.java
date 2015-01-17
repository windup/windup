package org.jboss.windup.reporting.rules.rendering;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PostFinalize;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.performance.RulePhaseExecutionStatisticsModel;
import org.jboss.windup.graph.model.performance.RuleProviderExecutionStatisticsModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.RuleProviderExecutionStatisticsService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Produces a simple text report of how long each RuleProvider's rule took to execute, the time spent in each phase, and any other timing data that
 * was been stored in {@link ExecutionStatistics}.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class ExecutionTimeReportRuleProvider extends WindupRuleProvider
{
    @Override
    public Class<? extends RulePhase> getPhase()
    {
        // this is basically a reporting rule, but we execute it during finalize in order
        // to also report on the time it took to generate reports
        return PostFinalize.class;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new GraphOperation()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context)
                        {
                            WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(event
                                        .getGraphContext());
                            String outputDir = cfg.getOutputPath().getFilePath();

                            // create a directory for the output
                            Path statsDir = Paths.get(outputDir, "stats");
                            FileUtils.deleteQuietly(statsDir.toFile());
                            try
                            {
                                Files.createDirectories(statsDir);
                            }
                            catch (IOException e)
                            {
                                throw new WindupException("Error creating output folder: " + outputDir + " due to: "
                                            + e.getMessage(), e);
                            }

                            Path detailedExecutionStatsOutputPath = statsDir.resolve("detailed_stats.csv");
                            ExecutionStatistics.get().serializeTimingData(detailedExecutionStatsOutputPath);

                            Path ruleTimingOutputPath = statsDir.resolve("timing.txt");
                            try (FileWriter fw = new FileWriter(ruleTimingOutputPath.toFile()))
                            {

                                RuleProviderExecutionStatisticsService statsByRuleProviderService = new RuleProviderExecutionStatisticsService(
                                            event.getGraphContext());
                                Iterable<RuleProviderExecutionStatisticsModel> ruleProviderStatModels = statsByRuleProviderService
                                            .findAllOrderedByIndex();

                                // rule execution timings
                                fw.write("-----------------------------------------------------------\n");
                                fw.write("Rule execution timings:\n\n");
                                for (RuleProviderExecutionStatisticsModel model : ruleProviderStatModels)
                                {
                                    fw.write(model.getRuleProviderID());
                                    fw.write(": ");
                                    fw.write(String.valueOf(model.getTimeTaken()));
                                    fw.write(" ms");
                                    fw.write(" (");
                                    fw.write(String.valueOf(model.getTimeTaken() / 1000));
                                    fw.write(" seconds)");
                                    fw.write("\n");
                                }
                                fw.write("-----------------------------------------------------------\n\n");

                                // phase execution timings
                                fw.write("Phase execution timings:\n\n");
                                GraphService<RulePhaseExecutionStatisticsModel> statsByPhaseService = new GraphService<>(
                                            event.getGraphContext(), RulePhaseExecutionStatisticsModel.class);
                                Iterable<RulePhaseExecutionStatisticsModel> rulePhaseStatModelIterable = statsByPhaseService
                                            .findAll();
                                List<RulePhaseExecutionStatisticsModel> rulePhaseStatModelList = new ArrayList<>();
                                for (RulePhaseExecutionStatisticsModel model : rulePhaseStatModelIterable)
                                {
                                    rulePhaseStatModelList.add(model);
                                }
                                Collections.sort(rulePhaseStatModelList,
                                            new Comparator<RulePhaseExecutionStatisticsModel>()
                                            {
                                                @Override
                                                public int compare(RulePhaseExecutionStatisticsModel o1,
                                                            RulePhaseExecutionStatisticsModel o2)
                                                {
                                                    return o1.getOrderExecuted() - o2.getOrderExecuted();
                                                }
                                            });

                                for (RulePhaseExecutionStatisticsModel model : rulePhaseStatModelList)
                                {
                                    fw.write(model.getRulePhase());
                                    fw.write(": ");
                                    fw.write(String.valueOf(model.getTimeTaken()));
                                    fw.write(" ms");
                                    fw.write(" (");
                                    fw.write(String.valueOf(model.getTimeTaken() / 1000));
                                    fw.write(" seconds)");
                                    fw.write("\n");
                                }
                                fw.write("-----------------------------------------------------------\n");
                            }
                            catch (IOException e)
                            {
                                throw new WindupException("Error creating output file: " + ruleTimingOutputPath.toString()
                                            + " due to: " + e.getMessage(), e);
                            }

                        }

                        @Override
                        public String toString()
                        {
                            return "RenderRuleExecutionTimeReport";
                        }
                    });
    }
}
