package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleUtils;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.RuleProviderRegistry;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.reporting.data.dto.RuleDto;
import org.jboss.windup.reporting.data.dto.TechnologyDto;
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
        Map<String, List<RuleDto>> result = new HashMap<>();

        RuleProviderRegistry.instance(event).getProviders().forEach(ruleProvider -> {
            if (ruleProvider instanceof AbstractRuleProvider) {
                String phase = ruleProvider.getMetadata().getPhase().getSimpleName();

                List<TechnologyDto> sourceTechnology = ruleProvider.getMetadata().getSourceTechnologies().stream().map(technologyReference -> {
                    TechnologyDto technologyDto = new TechnologyDto();
                    technologyDto.id = technologyReference.getId();
                    technologyDto.versionRange = technologyReference.getVersionRangeAsString();
                    return technologyDto;
                }).collect(Collectors.toList());
                List<TechnologyDto> targetTechnology = ruleProvider.getMetadata().getTargetTechnologies().stream().map(technologyReference -> {
                    TechnologyDto technologyDto = new TechnologyDto();
                    technologyDto.id = technologyReference.getId();
                    technologyDto.versionRange = technologyReference.getVersionRangeAsString();
                    return technologyDto;
                }).collect(Collectors.toList());

                List<RuleDto> rules = RuleExecutionResultsListener.instance(event)
                        .getRuleExecutionInformation((AbstractRuleProvider) ruleProvider)
                        .stream()
                        .filter(Objects::nonNull)
                        .map(ruleExecutionInformation -> {
                            RuleDto ruleDto = new RuleDto();

                            ruleDto.id = ruleExecutionInformation.getRule().getId();
                            ruleDto.content = RuleUtils.ruleToRuleContentsString(ruleExecutionInformation.getRule(), 0);
                            ruleDto.verticesAdded = ruleExecutionInformation.getVertexIDsAdded();
                            ruleDto.edgesAdded = ruleExecutionInformation.getEdgeIDsAdded();
                            ruleDto.verticesRemoved = ruleExecutionInformation.getVertexIDsRemoved();
                            ruleDto.edgesRemoved = ruleExecutionInformation.getEdgeIDsRemoved();
                            ruleDto.executed = ruleExecutionInformation.isExecuted();
                            ruleDto.failed = ruleExecutionInformation.isFailed();
                            ruleDto.failureMessage = ruleExecutionInformation.getFailureCause() != null && ruleExecutionInformation.getFailureCause().getMessage() != null ? ruleExecutionInformation.getFailureCause().getMessage() : null;
                            ruleDto.sourceTechnology = sourceTechnology;
                            ruleDto.targetTechnology = targetTechnology;

                            return ruleDto;
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
}
