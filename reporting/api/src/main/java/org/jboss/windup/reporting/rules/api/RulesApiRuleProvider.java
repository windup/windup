package org.jboss.windup.reporting.rules.api;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleUtils;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.RuleProviderRegistry;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.reporting.ruleexecution.RuleExecutionResultsListener;
import org.jboss.windup.reporting.rules.AttachApplicationReportsToIndexRuleProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RuleMetadata(
        phase = PostReportGenerationPhase.class,
        before = AttachApplicationReportsToIndexRuleProvider.class,
        haltOnException = true
)
public class RulesApiRuleProvider extends AbstractApiRuleProvider {

    @Override
    public String getOutputFilename() {
        return "rules.json";
    }

    @Override
    public Object getData(GraphRewrite event) {
        Map<String, List<RuleData>> result = new HashMap<>();

        RuleProviderRegistry.instance(event).getProviders().forEach(ruleProvider -> {
            if (ruleProvider instanceof AbstractRuleProvider) {
                String phase = ruleProvider.getMetadata().getPhase().getSimpleName();
                List<RuleData> rules = RuleExecutionResultsListener.instance(event)
                        .getRuleExecutionInformation((AbstractRuleProvider) ruleProvider)
                        .stream()
                        .filter(Objects::nonNull)
                        .map(ruleExecutionInformation -> {
                            RuleData ruleData = new RuleData();

                            ruleData.id = ruleExecutionInformation.getRule().getId();
                            ruleData.content = RuleUtils.ruleToRuleContentsString(ruleExecutionInformation.getRule(), 0);
                            ruleData.verticesAdded = ruleExecutionInformation.getVertexIDsAdded();
                            ruleData.edgesAdded = ruleExecutionInformation.getEdgeIDsAdded();
                            ruleData.verticesRemoved = ruleExecutionInformation.getVertexIDsRemoved();
                            ruleData.edgesRemoved = ruleExecutionInformation.getEdgeIDsRemoved();
                            ruleData.executed = ruleExecutionInformation.isExecuted();
                            ruleData.failed = ruleExecutionInformation.isFailed();
                            ruleData.failureMessage = ruleExecutionInformation.getFailureCause() != null && ruleExecutionInformation.getFailureCause().getMessage() != null ? ruleExecutionInformation.getFailureCause().getMessage() : null;

                            return ruleData;
                        })
                        .collect(Collectors.toList());

                if (!result.containsKey(phase)) {
                    result.put(phase, new ArrayList<>());
                }
                result.get(phase).addAll(rules);
            }
        });

        return result;
    }

    static class RuleData {
        public String id;
        public String content;
        public Integer verticesAdded;
        public Integer verticesRemoved;
        public Integer edgesAdded;
        public Integer edgesRemoved;
        public boolean executed;
        public boolean failed;
        public String failureMessage;
    }
}
