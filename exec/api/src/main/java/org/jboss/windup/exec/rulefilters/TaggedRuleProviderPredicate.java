package org.jboss.windup.exec.rulefilters;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.PostMigrationRulesPhase;

/**
 * Accepts the given provider if it has any or all of requested include tags, or has not all or any of the requested
 * exclude tags.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>, ozizka@redhat.com
 */
public class TaggedRuleProviderPredicate implements Predicate<RuleProvider> {
    private final Set<String> includeTags;
    private final Set<String> excludeTags;
    private boolean requireAllIncludeTags = false;
    private boolean requireAllExcludeTags = false;

    /**
     * Creates the {@link TaggedRuleProviderPredicate} with the given include and excludes.
     */
    public TaggedRuleProviderPredicate(Collection<String> includeTags, Collection<String> excludeTags) {
        Set<String> emptySet = Collections.emptySet();
        this.includeTags = includeTags == null ? emptySet : new HashSet<>(includeTags);
        this.excludeTags = excludeTags == null ? emptySet : new HashSet<>(excludeTags);
    }

    /**
     * Sets the rule to require all of the include tags. If this value is true, then a {@link RuleProvider} must have
     * all of the tags in the include list in order to be matched. If it is false, then having a single tag match is
     * sufficient.
     * <p>
     * The default value is false.
     */
    public TaggedRuleProviderPredicate setRequireAllIncludeTags(boolean requireAll) {
        this.requireAllIncludeTags = requireAll;
        return this;
    }

    /**
     * <p>
     * Sets the rule to require all of the exclude tags. If this value is false (the default), then this
     * {@link Predicate} will reject any {@link RuleProvider}s that have a tag that is also in the excludeTags list.
     * </p>
     * <p>
     * If this value is true, then it will reject only providers that have all of the tags in the exclude list.
     * </p>
     */
    public TaggedRuleProviderPredicate setRequireAllExcludeTags(boolean requireAll) {
        this.requireAllExcludeTags = requireAll;
        return this;
    }

    @Override
    public boolean accept(RuleProvider provider) {
        Set<String> tags = provider.getMetadata().getTags();

        if (!(provider.getMetadata().getPhase().isInstance(new MigrationRulesPhase()) ||
                provider.getMetadata().getPhase().isInstance(new PostMigrationRulesPhase()))){
            return true;
        }

        boolean result = true;
        if (!includeTags.isEmpty()) {
            if (requireAllIncludeTags)
                result = tags.containsAll(includeTags);
            else
                result = CollectionUtils.containsAny(tags, includeTags);
        }

        if (result && !excludeTags.isEmpty()) {
            if (requireAllExcludeTags)
                result = !tags.containsAll(excludeTags);
            else
                result = !CollectionUtils.containsAny(tags, excludeTags);
        }

        return result;
    }


    @Override
    public String toString() {
        return TaggedRuleProviderPredicate.class.getSimpleName() + "{incl " + includeTags.size() + ", excl " + excludeTags.size() + ", requireAllIncl=" + requireAllIncludeTags + ", requireAllExcl=" + requireAllExcludeTags + '}';
    }

}
