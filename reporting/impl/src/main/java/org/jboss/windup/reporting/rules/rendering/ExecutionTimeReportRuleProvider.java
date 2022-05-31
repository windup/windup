package org.jboss.windup.reporting.rules.rendering;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PostFinalizePhase;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.performance.RulePhaseExecutionStatisticsModel;
import org.jboss.windup.graph.model.performance.RuleProviderExecutionStatisticsModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.RuleProviderExecutionStatisticsService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Produces a simple text report of how long each RuleProvider's rule took to execute, the time spent in each phase, and
 * any other timing data that was been stored in {@link ExecutionStatistics}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * <p>
 * This is basically a reporting rule, but we execute it during finalize in order to also report on
 * the time it took to generate reports.
 */
@RuleMetadata(phase = PostFinalizePhase.class)
public class ExecutionTimeReportRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                        String outputDir = cfg.getOutputPath().getFilePath();

                        // create a directory for the output
                        Path statsDir = Paths.get(outputDir, "stats");
                        FileUtils.deleteQuietly(statsDir.toFile());
                        PathUtil.createDirectory(statsDir, "stats folder");

                        Path detailedExecutionStatsOutputPath = statsDir.resolve("detailed_stats.csv");
                        ExecutionStatistics.get().serializeTimingData(detailedExecutionStatsOutputPath);

                        Path ruleTimingOutputPath = statsDir.resolve("timing.txt");
                        try (FileWriter fw = new FileWriter(ruleTimingOutputPath.toFile())) {
                            Iterable<RuleProviderExecutionStatisticsModel> ruleProviderStatModels =
                                    new RuleProviderExecutionStatisticsService(event.getGraphContext()).findAllOrderedByIndex();

                            // rule execution timings
                            fw.write("-----------------------------------------------------------\n");
                            fw.write("Rule execution timings:\n\n");
                            for (RuleProviderExecutionStatisticsModel model : ruleProviderStatModels) {
                                int ms = model.getTimeTaken();
                                fw.write(String.format("% 5d.%03d, %s\n", ms / 1000, ms % 1000, model.getRuleProviderID()));
                            }
                            fw.write("-----------------------------------------------------------\n\n");

                            // phase execution timings
                            fw.write("Phase execution timings:\n\n");
                            GraphService<RulePhaseExecutionStatisticsModel> statsByPhaseService =
                                    new GraphService<>(event.getGraphContext(), RulePhaseExecutionStatisticsModel.class);
                            Iterable<RulePhaseExecutionStatisticsModel> rulePhaseStatModelIterable = statsByPhaseService.findAll();
                            List<RulePhaseExecutionStatisticsModel> rulePhaseStatModelList = new ArrayList<>();
                            for (RulePhaseExecutionStatisticsModel model : rulePhaseStatModelIterable) {
                                rulePhaseStatModelList.add(model);
                            }
                            Collections.sort(rulePhaseStatModelList, RulePhaseExecutionStatisticsModel.BY_ORDER_EXECUTED);

                            for (RulePhaseExecutionStatisticsModel model : rulePhaseStatModelList) {
                                int ms = model.getTimeTaken();
                                fw.write(String.format("% 6d.%03d, %s\n", ms / 1000, ms % 1000, model.getRulePhase()));
                            }
                            fw.write("-----------------------------------------------------------\n");
                        } catch (IOException e) {
                            throw new WindupException("Error creating output file: " + ruleTimingOutputPath
                                    + " due to: " + e.getMessage(), e);
                        }

                    }

                    @Override
                    public String toString() {
                        return "RenderRuleExecutionTimeReport";
                    }
                });
    }
}
