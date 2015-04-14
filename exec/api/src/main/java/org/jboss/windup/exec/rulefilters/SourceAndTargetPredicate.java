package org.jboss.windup.exec.rulefilters;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.TechnologyReference;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class SourceAndTargetPredicate implements Predicate<RuleProvider>
{
    private final Set<String> sources;
    private final Set<String> targets;

    public SourceAndTargetPredicate(Collection<String> sources, Collection<String> targets)
    {
        this.sources = initSet(sources);
        this.targets = initSet(targets);
    }

    @Override
    public boolean accept(RuleProvider type)
    {
        Set<TechnologyReference> providerSources = type.getMetadata().getSourceTechnologies();
        Set<TechnologyReference> providerTargets = type.getMetadata().getTargetTechnologies();

        return (techMatches(sources, providerSources) && techMatches(targets, providerTargets));
    }

    private boolean techMatches(Set<String> techs, Set<TechnologyReference> technologyReferences)
    {
        if (techs.isEmpty() || technologyReferences.isEmpty())
        {
            return true;
        }

        for (TechnologyReference technologyReference : technologyReferences)
        {
            if (techs.contains(technologyReference.getId()))
            {
                return true;
            }
        }

        return false;
    }

    private Set<String> initSet(Collection<String> values)
    {
        if (values == null)
        {
            return Collections.emptySet();
        }
        else
        {
            return new HashSet<>(values);
        }
    }
}
