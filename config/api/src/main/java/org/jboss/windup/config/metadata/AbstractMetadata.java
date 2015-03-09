package org.jboss.windup.config.metadata;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.util.Strings;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.RulePhase;
import org.ocpsoft.rewrite.context.ContextBase;

/**
 * Base class for constructing {@link RulesetMetadata} instances. Provides sensible defaults.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class AbstractMetadata extends ContextBase implements RulesetMetadata
{
    public static final Class<? extends RulePhase> DEFAULT_PHASE = MigrationRulesPhase.class;

    private String id;

    /**
     * Construct a new {@link AbstractMetadata} instance using the given {@link String} ID.
     */
    public AbstractMetadata(String id)
    {
        Assert.notNull(id, "Ruleset ID must not be null.");
        this.id = id;
    }

    @Override
    public String getID()
    {
        return id;
    }

    @Override
    public String getOrigin()
    {
        return getID();
    }

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return DEFAULT_PHASE;
    }

    @Override
    public List<Class<? extends RuleProvider>> getExecuteAfter()
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> getExecuteAfterIDs()
    {
        return Collections.emptyList();
    }

    @Override
    public List<Class<? extends RuleProvider>> getExecuteBefore()
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> getExecuteBeforeIDs()
    {
        return Collections.emptyList();
    }

    @Override
    public Set<String> getTags()
    {
        return Collections.emptySet();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractMetadata other = (AbstractMetadata) obj;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public boolean hasTags(String tag, String... tags)
    {
        Set<String> expected = new HashSet<>();
        if (!Strings.isNullOrEmpty(tag))
            expected.add(tag);

        if (tags != null)
        {
            for (String t : tags)
            {
                if (!Strings.isNullOrEmpty(tag))
                    expected.add(t);
            }
        }

        return getTags().containsAll(expected);
    }
}
