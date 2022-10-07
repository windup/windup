package org.jboss.windup.reporting.rules.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.RuleProviderRegistry;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PostFinalizePhase;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.rule.ExecutionPhaseModel;
import org.jboss.windup.reporting.model.rule.RuleExecutionModel;
import org.jboss.windup.reporting.model.rule.RuleProviderModel;
import org.jboss.windup.reporting.ruleexecution.RuleExecutionInformation;
import org.jboss.windup.reporting.ruleexecution.RuleExecutionResultsListener;
import org.jboss.windup.reporting.service.rule.ExecutionPhaseService;
import org.jboss.windup.reporting.service.rule.RuleExecutionService;
import org.jboss.windup.reporting.service.rule.RuleProviderService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Stores information about executed rules into graph
 *
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@RuleMetadata(phase = PostFinalizePhase.class)
public class CreateRuleProviderReportRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        GraphOperation addRuleReports = new GraphOperation() {
            Map<String, ExecutionPhaseModel> phaseModelMap;

            GraphContext graphContext;

            RuleProviderService ruleProviderService;
            RuleExecutionService ruleExecutionService;
            ExecutionPhaseService executionPhaseService;

            private void initialize(GraphContext graphContext) {
                this.graphContext = graphContext;
                this.ruleProviderService = new RuleProviderService(this.graphContext);
                this.ruleExecutionService = new RuleExecutionService(this.graphContext);
                this.executionPhaseService = new ExecutionPhaseService(this.graphContext);

                this.phaseModelMap = new HashMap<>();
            }

            @Override
            public void perform(GraphRewrite event, EvaluationContext context) {
                this.initialize(event.getGraphContext());

                List<RuleProvider> ruleProviderList = RuleProviderRegistry.instance(event).getProviders();

                for (RuleProvider ruleProvider : ruleProviderList) {
                    if (ruleProvider instanceof RulePhase) {
                        this.addPhase(ruleProvider.getMetadata().getID());
                        continue;
                    }

                    RuleProviderModel ruleProviderModel = this.ruleProviderService.create();
                    ruleProviderModel.setRuleProviderID(ruleProvider.getMetadata().getID());

                    ExecutionPhaseModel executionPhaseModel = this.getPhaseModel(ruleProvider);
                    executionPhaseModel.addRuleProvider(ruleProviderModel);

                    List<RuleExecutionInformation> ruleProviderInfo = RuleExecutionResultsListener.instance(event)
                            .getRuleExecutionInformation((AbstractRuleProvider) ruleProvider);

                    for (RuleExecutionInformation ruleInfo : ruleProviderInfo) {
                        if (ruleInfo == null)
                            continue;

                        RuleExecutionModel ruleExecutionModel = this.ruleExecutionService.create();
                        ruleExecutionModel.setDataFromRuleInfo(ruleInfo);
                        ruleProviderModel.addRule(ruleExecutionModel);
                    }
                }

                this.graphContext.commit();
            }

            private void addPhase(String name) {
                if (!this.phaseModelMap.containsKey(name)) {
                    ExecutionPhaseModel phaseModel = executionPhaseService.create();
                    phaseModel.setName(name);
                    this.phaseModelMap.put(name, phaseModel);
                }
            }

            private ExecutionPhaseModel getPhaseModel(RuleProvider ruleProvider) {
                Class<? extends RulePhase> phase = ruleProvider.getMetadata().getPhase();

                String name = phase.getSimpleName();

                if (!this.phaseModelMap.containsKey(name)) {
                    ExecutionPhaseModel phaseModel = executionPhaseService.create();
                    phaseModel.setName(name);
                    this.phaseModelMap.put(name, phaseModel);
                }

                ExecutionPhaseModel phaseModel = this.phaseModelMap.get(name);

                return phaseModel;
            }

            @Override
            public String toString() {
                return "AddRuleReports";
            }
        };

        return ConfigurationBuilder.begin()
                .addRule()
                .perform(addRuleReports);
    }
}
