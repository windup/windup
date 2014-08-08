package org.jboss.windup.reporting.rules;

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
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.performance.RulePhaseExecutionStatisticsModel;
import org.jboss.windup.graph.model.performance.RuleProviderExecutionStatisticsModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.RuleProviderExecutionStatisticsService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Produces a simple text report of how long each RuleProvider's rule took to execute, as well as the amount of time
 * spent in each phase.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class RuleExecutionTimeReportRuleProvider extends WindupRuleProvider
{
    @Override
    public RulePhase getPhase()
    {
        // this is basically a reporting rule, but we execute it during finalize in order
        // to also report on the time it took to generate reports
        return RulePhase.POST_FINALIZE;
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
                            WindupConfigurationModel cfg = GraphService.getConfigurationModel(event.getGraphContext());
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

                            Path outputPath = statsDir.resolve("timing.txt");
                            try (FileWriter fw = new FileWriter(outputPath.toFile()))
                            {

                                RuleProviderExecutionStatisticsService statsByRuleProviderService = event
                                            .getGraphContext().getService(RuleProviderExecutionStatisticsModel.class);
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
                                GraphService<RulePhaseExecutionStatisticsModel> statsByPhaseService = event
                                            .getGraphContext().getService(RulePhaseExecutionStatisticsModel.class);
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
                                                    RulePhase r1 = RulePhase.valueOf(o1.getRulePhase());
                                                    RulePhase r2 = RulePhase.valueOf(o2.getRulePhase());
                                                    return r1.getPriority() - r2.getPriority();
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
                                throw new WindupException("Error creating output file: " + outputPath.toString()
                                            + " due to: " + e.getMessage(), e);
                            }

                        }
                    });
    }
}
