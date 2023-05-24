package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleUtils;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.RuleProviderRegistry;
import org.jboss.windup.config.phase.PreReportPfRenderingPhase;
import org.jboss.windup.reporting.data.dto.RuleContentDto;
import org.jboss.windup.reporting.data.dto.RuleDto;
import org.jboss.windup.reporting.data.rules.utils.DataUtils;
import org.jboss.windup.reporting.ruleexecution.RuleExecutionResultsListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RuleMetadata(
        phase = PreReportPfRenderingPhase.class,
        haltOnException = true
)
public class RulesRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "rules";

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        Map<String, List<RuleDto>> result = new HashMap<>();

        RuleProviderRegistry.instance(event).getProviders().forEach(ruleProvider -> {
            if (ruleProvider instanceof AbstractRuleProvider) {
                String ruleSetId = ruleProvider.getMetadata().getID();
                String phase = ruleProvider.getMetadata().getPhase().getSimpleName();

                List<RuleDto.TechnologyDto> sourceTechnology = ruleProvider.getMetadata().getSourceTechnologies().stream().map(technologyReference -> {
                    RuleDto.TechnologyDto technologyDto = new RuleDto.TechnologyDto();
                    technologyDto.setId(technologyReference.getId());
                    technologyDto.setVersionRange(technologyReference.getVersionRangeAsString());
                    return technologyDto;
                }).collect(Collectors.toList());
                List<RuleDto.TechnologyDto> targetTechnology = ruleProvider.getMetadata().getTargetTechnologies().stream().map(technologyReference -> {
                    RuleDto.TechnologyDto technologyDto = new RuleDto.TechnologyDto();
                    technologyDto.setId(technologyReference.getId());
                    technologyDto.setVersionRange(technologyReference.getVersionRangeAsString());
                    return technologyDto;
                }).collect(Collectors.toList());

                List<RuleDto> rules = RuleExecutionResultsListener.instance(event)
                        .getRuleExecutionInformation((AbstractRuleProvider) ruleProvider)
                        .stream()
                        .filter(Objects::nonNull)
                        .map(ruleExecutionInformation -> {
                            RuleDto ruleDto = new RuleDto();

                            ruleDto.setId(DataUtils.sanitizeFilename(ruleExecutionInformation.getRule().getId()));
                            ruleDto.setRuleSetId(ruleSetId);
                            ruleDto.setVerticesAdded(ruleExecutionInformation.getVertexIDsAdded());
                            ruleDto.setEdgesAdded(ruleExecutionInformation.getEdgeIDsAdded());
                            ruleDto.setVerticesRemoved(ruleExecutionInformation.getVertexIDsRemoved());
                            ruleDto.setEdgesRemoved(ruleExecutionInformation.getEdgeIDsRemoved());
                            ruleDto.setExecuted(ruleExecutionInformation.isExecuted());
                            ruleDto.setFailed(ruleExecutionInformation.isFailed());
                            ruleDto.setFailureMessage(ruleExecutionInformation.getFailureCause() != null && ruleExecutionInformation.getFailureCause().getMessage() != null ? ruleExecutionInformation.getFailureCause().getMessage() : null);
                            ruleDto.setSourceTechnology(sourceTechnology);
                            ruleDto.setTargetTechnology(targetTechnology);

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

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        Map<String, Object> result = new HashMap<>();
        RuleProviderRegistry.instance(event).getProviders().forEach(ruleProvider -> {
            if (ruleProvider instanceof AbstractRuleProvider) {
                RuleExecutionResultsListener.instance(event)
                        .getRuleExecutionInformation((AbstractRuleProvider) ruleProvider)
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(ruleExecutionInformation -> {
                            RuleContentDto ruleDto = new RuleContentDto();

                            ruleDto.setId(DataUtils.sanitizeFilename(ruleExecutionInformation.getRule().getId()));
                            ruleDto.setContent(RuleUtils.ruleToRuleContentsString(ruleExecutionInformation.getRule(), 0));

                            result.put(ruleDto.getId(), ruleDto);
                        });
            }
        });

        return result;
    }
}
