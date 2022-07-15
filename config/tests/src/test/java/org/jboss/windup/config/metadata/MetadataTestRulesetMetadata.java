package org.jboss.windup.config.metadata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.versions.Versions;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class MetadataTestRulesetMetadata extends AbstractRulesetMetadata {
    public MetadataTestRulesetMetadata() {
        super("TEST-METADATA");
    }

    public Set<String> getTags() {
        return new HashSet<>(Arrays.asList("ruleset-meta-tag"));
    }

    @Override
    public Set<AddonId> getRequiredAddons() {
        return new HashSet<>(Arrays.asList(
                AddonId.from("foo", "1"),
                AddonId.from("bar", "2"),
                AddonId.from("baz", "3")
        ));
    }

    @Override
    public Set<TechnologyReference> getSourceTechnologies() {
        return new HashSet<>(Arrays.asList(
                new TechnologyReference("source-a", Versions.parseVersionRange("[1,]")),
                new TechnologyReference("source-b", Versions.parseVersionRange("[2,]"))
        ));
    }

    @Override
    public Set<TechnologyReference> getTargetTechnologies() {
        return new HashSet<>(Arrays.asList(
                new TechnologyReference("target-x", Versions.parseVersionRange("[3,]"))
        ));
    }
}
