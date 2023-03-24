package org.jboss.windup.reporting.config;

import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.graph.model.TechnologyReferenceModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class TechnologiesIntersector {

    public static List<TechnologyReferenceModel> extractSourceTechnologies(WindupConfigurationModel configuration, RuleProviderMetadata ruleProviderMetadata) {
        List<TechnologyReferenceModel> configuredSourceTechnologies = configuration.getSourceTechnologies();
        Set<TechnologyReference> ruleSourceTechnologies = ruleProviderMetadata.getSourceTechnologies();

        return intersectTechnologies(configuredSourceTechnologies, ruleSourceTechnologies);
    }

    public static List<TechnologyReferenceModel> extractTargetTechnologies(WindupConfigurationModel configuration, RuleProviderMetadata ruleProviderMetadata) {
        List<TechnologyReferenceModel> configuredTargetTechnologies = configuration.getTargetTechnologies();
        Set<TechnologyReference> ruleTargetTechnologies = ruleProviderMetadata.getTargetTechnologies();

        return intersectTechnologies(configuredTargetTechnologies, ruleTargetTechnologies);
    }

    private static List<TechnologyReferenceModel> intersectTechnologies(Collection<TechnologyReferenceModel> configuredTechnologies, Collection<TechnologyReference> ruleTechnologies) {
        return configuredTechnologies.stream()
                .filter(confTech -> {
                    Set<TechnologyReference> confTechsWithSameId = ruleTechnologies.stream().filter(ruleTech -> confTech.getTechnologyID().equals(ruleTech.getId())).collect(toSet());
                    return !confTechsWithSameId.isEmpty();
                })
                .filter(confTech -> ruleTechnologies.stream().filter(ruleTech -> {
                    if (ruleTech.getId().equals(confTech.getTechnologyID())) {
                        TechnologyReference confTechRef = new TechnologyReference(confTech);
                        return !confTechRef.versionRangesOverlap(ruleTech.getVersionRange());
                    }
                    return false;
                }).collect(toSet()).isEmpty())
                .collect(toList());
    }
}
