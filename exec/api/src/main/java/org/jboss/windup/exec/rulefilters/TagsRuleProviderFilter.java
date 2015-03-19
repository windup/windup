package org.jboss.windup.exec.rulefilters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.collections.CollectionUtils;
import org.jboss.windup.config.RuleProvider;

/**
 * Accepts the given provider if it has any or all of requested include tags,
 * or has not all or any of the requested exclude tags.
 *
 * @author Ondrej Zizka, ozizka@redhat.com
 */
public class TagsRuleProviderFilter implements RuleProviderFilter
{
    private static Logger log = Logger.getLogger(TagsRuleProviderFilter.class.getName());

    private final Set<String> includeTags;
    private final Set<String> excludeTags;
    private boolean requireAllIncludeTags = false;
    private boolean requireAllExcludeTags = false;


    public TagsRuleProviderFilter(Collection<String> includeTags, Collection<String> excludeTags)
    {
        this.includeTags = includeTags == null ? null : new HashSet(includeTags);
        this.excludeTags = excludeTags == null ? null : new HashSet(excludeTags);
    }


    public TagsRuleProviderFilter setRequireAllIncludeTags(boolean requireAll)
    {
        this.requireAllIncludeTags = requireAll;
        return this;
    }

    public TagsRuleProviderFilter setRequireAllExcludeTags(boolean requireAll)
    {
        this.requireAllExcludeTags = requireAll;
        return this;
    }



    @Override
    public boolean accept(RuleProvider provider)
    {
        Set<String> tags = provider.getMetadata().getTags();

        boolean includeMatches =
                (this.includeTags == null)
                ||
                (this.requireAllIncludeTags
                ? tags.containsAll(this.includeTags)
                : CollectionUtils.containsAny(tags, this.includeTags));

        if (!includeMatches)
            return false;

        boolean excludeMatches =
                (this.excludeTags == null)
                ||
                (this.requireAllExcludeTags
                ? !tags.containsAll(this.excludeTags)
                : !CollectionUtils.containsAny(tags, this.excludeTags));

        return includeMatches && excludeMatches;
    }

}