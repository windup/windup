package org.jboss.windup.config.metadata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.util.Strings;

/**
 * Base class for constructing {@link RulesetMetadata} instances. Provides sensible defaults.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class AbstractRulesetMetadata implements RulesetMetadata {
    private final String id;

    /**
     * Construct a new {@link AbstractRulesetMetadata} instance using the given {@link String} ID.
     */
    public AbstractRulesetMetadata(String id) {
        Assert.notNull(id, "Ruleset ID must not be null.");
        this.id = id;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getOrigin() {
        return getClass().getClassLoader().toString();
    }

    @Override
    public Set<String> getTags() {
        return Collections.emptySet();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractRulesetMetadata other = (AbstractRulesetMetadata) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public boolean hasTags(String tag, String... tags) {
        Set<String> expected = new HashSet<>();
        if (!Strings.isNullOrEmpty(tag))
            expected.add(tag);

        if (tags != null) {
            for (String t : tags) {
                if (!Strings.isNullOrEmpty(tag))
                    expected.add(t);
            }
        }

        return getTags().containsAll(expected);
    }

    @Override
    public Set<TechnologyReference> getSourceTechnologies() {
        return Collections.emptySet();
    }

    @Override
    public Set<TechnologyReference> getTargetTechnologies() {
        return Collections.emptySet();
    }

    @Override
    public Set<AddonId> getRequiredAddons() {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return "RulesetMetadata ["
                + "\tid=" + id + ", "
                + "\tdescription=" + getDescription() + ", "
                + "\torigin=" + getOrigin() + ", "
                + "\ttags=" + getTags() + ", "
                + "\tsourceTechnologies=" + getSourceTechnologies() + ", "
                + "\ttargetTechnologies=" + getTargetTechnologies() + ""
                + "]";
    }

}
