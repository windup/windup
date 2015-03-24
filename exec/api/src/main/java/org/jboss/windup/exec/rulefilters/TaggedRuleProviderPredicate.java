package org.jboss.windup.exec.rulefilters;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;

/**
 * Accepts the given provider if it has any or all of requested include tags, or has not all or any of the requested exclude tags.
 *
 * @author Ondrej Zizka, ozizka@redhat.com
 */
public class TaggedRuleProviderPredicate implements Predicate<RuleProvider>
{
    private static Logger log = Logger.getLogger(TaggedRuleProviderPredicate.class.getName());

    private final Set<String> includeTags;
    private final Set<String> excludeTags;
    private boolean requireAllIncludeTags = false;
    private boolean requireAllExcludeTags = false;

    /**
     * Creates the {@link TaggedRuleProviderPredicate} with the given include and excludes.
     */
    public TaggedRuleProviderPredicate(Collection<String> includeTags, Collection<String> excludeTags)
    {
        Set<String> emptySet = Collections.emptySet();
        this.includeTags = includeTags == null ? emptySet : new HashSet<>(includeTags);
        this.excludeTags = excludeTags == null ? emptySet : new HashSet<>(excludeTags);
    }

    /**
     * Sets the rule to require all of the include tags. If this value is true, then a {@link RuleProvider} must have all of the tags in the include
     * list in order to be matched. If it is false, then having a single tag match is sufficient.
     *
     * The default value is false.
     */
    public TaggedRuleProviderPredicate setRequireAllIncludeTags(boolean requireAll)
    {
        this.requireAllIncludeTags = requireAll;
        return this;
    }

    /**
     * <p>
     * Sets the rule to require all of the exclude tags. If this value is false (the default), then this {@link Predicate} will reject any
     * {@link RuleProvider}s that have a tag that is also in the excludeTags list.
     * </p>
     * <p>
     * If this value is true, then it will reject only providers that have all of the tags in the exclude list.
     * </p>
     */
    public TaggedRuleProviderPredicate setRequireAllExcludeTags(boolean requireAll)
    {
        this.requireAllExcludeTags = requireAll;
        return this;
    }

    @Override
    public boolean accept(RuleProvider provider)
    {
        Set<String> tags = provider.getMetadata().getTags();

        boolean includeMatches =
                    (this.includeTags.isEmpty())
                                ||
                                (this.requireAllIncludeTags
                                            ? tags.containsAll(this.includeTags)
                                            : CollectionUtils.containsAny(tags, this.includeTags));

        if (!includeMatches)
            return false;

        boolean excludeMatches =
                    (this.excludeTags.isEmpty())
                                ||
                                (this.requireAllExcludeTags
                                            ? !tags.containsAll(this.excludeTags)
                                            : !CollectionUtils.containsAny(tags, this.excludeTags));

        return includeMatches && excludeMatches;
    }

}