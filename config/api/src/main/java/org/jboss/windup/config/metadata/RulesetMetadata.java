package org.jboss.windup.config.metadata;

import java.util.Set;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.windup.config.RuleProvider;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Each {@link Addon} that contains {@link RuleProvider} implementations should implement this interface, and by doing
 * so provide some basic metadata about its contents.
 *
 * @author Jess Sightler <jesse.sightler@gmail.com>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface RulesetMetadata {
    /**
     * Returns a unique identifier for the corresponding {@link RuleProvider}. The default is based on the originating
     * {@link Addon} and {@link Class} name, but this can be overridden in subclasses to provide a more readable name.
     */
    String getID();

    /**
     * Returns a descriptive {@link String}, informing a human where they can find the {@link Rule} instances provided
     * by this {@link RuleProvider}.
     */
    String getOrigin();

    /**
     * Returns a human-readable description the rules associated with this {@link RulesetMetadata}.
     */
    String getDescription();

    /**
     * Return the {@link Set} of tags by which this {@link RulesetMetadata} is classified.
     */
    Set<String> getTags();

    /**
     * Return <code>true</code> if this {@link RulesetMetadata} contains all of the given tags.
     */
    boolean hasTags(String tag, String... tags);

    /**
     * Return the {@link Set} of source {@link TechnologyReference} instances to which this {@link RuleProvider} is
     * related.
     */
    Set<TechnologyReference> getSourceTechnologies();

    /**
     * Return the {@link Set} of target {@link TechnologyReference} instances to which this {@link RuleProvider} is
     * related.
     */
    Set<TechnologyReference> getTargetTechnologies();

    /**
     * Return the {@link Set} of {@link Addon}s required to run this rule-set. (<b>Note:</b> This is typically only used
     * in situations where rules are provided externally - such as XML - whereas in Java, the {@link Addon} will already
     * define its dependencies on other addons directly.)
     */
    Set<AddonId> getRequiredAddons();
}
