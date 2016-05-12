package org.jboss.windup.config.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.util.Annotations;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.versions.EmptyVersionRange;
import org.jboss.forge.furnace.versions.Versions;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleProviderLoader;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Fluent builder for creating {@link RuleProviderMetadata} instances. Provides sensible defaults using given required values.
 * <p>
 * If {@link RulesetMetadata} is available in the {@link Addon} in which this {@link MetadataBuilder} was constructed, this will inherit values from
 * {@link RulesetMetadata} for {@link #getTags()}, {@link #getSourceTechnologies()}, {@link #getTargetTechnologies()} and {@link #getRequiredAddons()}.
 * <p>
 * Inherited metadata is specified by {@link #setRulesetMetadata(RulesetMetadata)}, and is typically performed by the {@link RuleProviderLoader}
 * implementation.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class MetadataBuilder extends AbstractRulesetMetadata implements RuleProviderMetadata
{
    private static final Logger LOG = Logging.get(MetadataBuilder.class);
    public static final Class<? extends RulePhase> DEFAULT_PHASE = MigrationRulesPhase.class;

    private Class<? extends RuleProvider> implementationType;
    private String origin;
    private Class<? extends RulePhase> phase;

    private List<Class<? extends RuleProvider>> executeAfter = new ArrayList<>();
    private List<String> executeAfterIDs = new ArrayList<>();
    private List<Class<? extends RuleProvider>> executeBefore = new ArrayList<>();
    private List<String> executeBeforeIDs = new ArrayList<>();
    private String description;
    private Set<String> tags = new HashSet<>();
    private final Set<TechnologyReference> sourceTechnologies = new HashSet<>();
    private final Set<TechnologyReference> targetTechnologies = new HashSet<>();
    private final Set<AddonId> requiredAddons = new HashSet<>();
    private boolean haltOnException = false;
    private boolean overrideProvider = false;
    private boolean disabled = false;

    private RulesetMetadata parent = new AbstractRulesetMetadata("NULL");

    private MetadataBuilder(Class<? extends RuleProvider> implementationType, String providerId)
    {
        super(providerId);
        this.implementationType = implementationType;
    }

    /**
     * Create a new {@link RuleProviderMetadata} builder instance for the given {@link RuleProvider} type, using the provided parameters and
     * {@link RulesetMetadata} to seed sensible defaults.
     */
    public static MetadataBuilder forProvider(Class<? extends RuleProvider> implementationType)
    {
        String id = implementationType.getSimpleName();

        RuleMetadata metadata = Annotations.getAnnotation(implementationType, RuleMetadata.class);
        if (metadata != null && !metadata.id().isEmpty())
            id = metadata.id();

        return forProvider(implementationType, id);
    }

    /**
     * Create a new {@link RuleProviderMetadata} builder instance for the given {@link RuleProvider} type, and {@link String} ID, using the provided
     * parameters and {@link RulesetMetadata} to seed sensible defaults.
     */
    public static MetadataBuilder forProvider(Class<? extends RuleProvider> implementationType, String providerId)
    {
        Assert.notNull(implementationType, "Rule provider Implementation type must not be null.");
        Assert.notNull(providerId, "Rule provider ID must not be null.");

        MetadataBuilder builder = new MetadataBuilder(implementationType, providerId)
            .setOrigin(implementationType.getName() + " loaded from " + implementationType.getClassLoader().toString());

        RuleMetadata metadata = Annotations.getAnnotation(implementationType, RuleMetadata.class);
        if (metadata == null)
            return builder;

        builder.setOverrideProvider(metadata.overrideProvider());

        if (StringUtils.isNotBlank(metadata.description()))
            builder.setDescription(metadata.description());

        Class<? extends RuleProvider>[] after = metadata.after();
        if (after.length > 0)
            builder.setExecuteAfter(Arrays.asList(after));

        String[] afterIDs = metadata.afterIDs();
        if (afterIDs.length > 0)
            builder.setExecuteAfterIDs(Arrays.asList(afterIDs));

        Class<? extends RuleProvider>[] before = metadata.before();
        if (before.length > 0)
            builder.setExecuteBefore(Arrays.asList(before));

        String[] beforeIDs = metadata.beforeIDs();
        if (beforeIDs.length > 0)
            builder.setExecuteBeforeIDs(Arrays.asList(beforeIDs));

        builder.setPhase(metadata.phase());

        String[] tags = metadata.tags();
        if (tags.length > 0)
            builder.setTags(Arrays.asList(tags));

        Technology[] sourceTechnologies = metadata.sourceTechnologies();
        if (sourceTechnologies.length > 0)
        {
            for (Technology technology : sourceTechnologies)
            {
                builder.addSourceTechnology(new TechnologyReference(
                            technology.id(),
                            "".equals(technology.versionRange().trim())
                                    ? new EmptyVersionRange()
                                    : Versions.parseVersionRange(technology.versionRange())));
            }
        }

        Technology[] targetTechnologies = metadata.targetTechnologies();
        if (targetTechnologies.length > 0)
        {
            for (Technology technology : targetTechnologies)
            {
                builder.addTargetTechnology(new TechnologyReference(
                            technology.id(),
                            Versions.parseVersionRange(technology.versionRange())));
            }
        }

        builder.haltOnException = metadata.haltOnException();
        builder.disabled = metadata.disabled();
        if (builder.disabled)
            LOG.info("Disabled rule provider: " + providerId);

        return builder;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof MetadataBuilder)) return false;
        if (!super.equals(o)) return false;

        MetadataBuilder that = (MetadataBuilder) o;

        return overrideProvider == that.overrideProvider;

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (overrideProvider ? 1 : 0);
        return result;
    }

    @Override
    public Class<? extends RuleProvider> getType()
    {
        return implementationType;
    }

    @Override
    public RulesetMetadata getRulesetMetadata()
    {
        return parent;
    }

    public MetadataBuilder setRulesetMetadata(RulesetMetadata parent)
    {
        if (parent != null)
            this.parent = parent;

        return this;
    }

    @Override
    public boolean isOverrideProvider()
    {
        return overrideProvider;
    }

    /**
     * Sets whether or not this provider's rules should override rules from other providers
     * with the same ID.
     */
    public MetadataBuilder setOverrideProvider(boolean overrideProvider)
    {
        this.overrideProvider = overrideProvider;
        return this;
    }

    @Override
    public String getOrigin()
    {
        return origin == null ? super.getOrigin() : origin;
    }

    /**
     * Set the descriptive information indicating where the corresponding {@link RuleProvider} instance is located (eg, a path to an XML file on disk,
     * or an {@link Addon} coordinate and class name).
     */
    public MetadataBuilder setOrigin(String origin)
    {
        this.origin = origin;
        return this;
    }

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return phase == null ? DEFAULT_PHASE : phase;
    }

    /**
     * Set the {@link RulePhase} in which the {@link Rule} instances from the corresponding {@link RuleProvider} instance should be executed.
     * <p>
     * The default phase is {@link org.jboss.windup.config.phase.MigrationRulesPhase}.
     */
    public MetadataBuilder setPhase(Class<? extends RulePhase> phase)
    {
        this.phase = phase;
        return this;
    }

    @Override
    public List<Class<? extends RuleProvider>> getExecuteAfter()
    {
        return Collections.unmodifiableList(executeAfter);
    }

    /**
     * Set the list of {@link RuleProvider} classes that should execute before the {@link Rule} instances in the corresponding {@link RuleProvider}
     * instance.
     *
     * <p>
     * {@link RuleProvider} references can also be specified based on id ({@link #getExecuteAfterIDs}).
     */
    public MetadataBuilder setExecuteAfter(List<Class<? extends RuleProvider>> executeAfter)
    {
        this.executeAfter = new ArrayList<>(executeAfter);
        return this;
    }

    /**
     * Ad an entry to the list of {@link RuleProvider} classes that should execute after the {@link Rule} instances in the corresponding
     * {@link RuleProvider} instance.
     *
     * {@link RuleProvider}s can also be specified based on id ({@link #getExecuteBeforeIDs}).
     */
    public MetadataBuilder addExecuteAfter(Class<? extends RuleProvider> type)
    {
        if (type != null)
        {
            executeAfter.add(type);
        }
        return this;
    }

    /**
     * Sets the human readable description.
     */
    public MetadataBuilder setDescription(String description)
    {
        this.description = description;
        return this;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public List<String> getExecuteAfterIDs()
    {
        return Collections.unmodifiableList(executeAfterIDs);
    }

    /**
     * Set the list of the {@link RuleProvider} classes that should execute before the {@link Rule} instances in the corresponding
     * {@link RuleProvider} instance.
     *
     * <p>
     * This is returned as a list of Rule IDs in order to support extensions that cannot depend on each other via class names. For example, in the
     * case of the Groovy rules extension, a single class covers many rules with their own IDs.
     *
     * For specifying Java-based rules, {@link #getExecuteAfter()} is preferred.
     */
    public MetadataBuilder setExecuteAfterIDs(List<String> executeAfterIDs)
    {
        this.executeAfterIDs = new ArrayList<>(executeAfterIDs);
        return this;
    }

    /**
     * Add an entry to the list of the {@link RuleProvider} classes that should execute before the {@link Rule} instances in the corresponding
     * {@link RuleProvider} instance.
     *
     * <p>
     * This is returned as a list of Rule IDs in order to support extensions that cannot depend on each other via class names. For example, in the
     * case of the Groovy rules extension, a single class covers many rules with their own IDs.
     *
     * For specifying Java-based rules, {@link #getExecuteAfter()} is preferred.
     */
    public MetadataBuilder addExecuteAfterId(String id)
    {
        if (id != null)
        {
            executeAfterIDs.add(id);
        }
        return this;
    }

    @Override
    public List<Class<? extends RuleProvider>> getExecuteBefore()
    {
        return Collections.unmodifiableList(executeBefore);
    }

    /**
     * Set the list of {@link RuleProvider} classes that should execute after the {@link Rule} instances in the corresponding {@link RuleProvider}
     * instance.
     *
     * {@link RuleProvider}s can also be specified based on id ({@link #getExecuteBeforeIDs}).
     */
    public MetadataBuilder setExecuteBefore(List<Class<? extends RuleProvider>> executeBefore)
    {
        this.executeBefore = new ArrayList<>(executeBefore);
        return this;
    }

    /**
     * Ad an entry to the list of {@link RuleProvider} classes that should execute after the {@link Rule} instances in the corresponding
     * {@link RuleProvider} instance.
     *
     * {@link RuleProvider}s can also be specified based on id ({@link #getExecuteBeforeIDs}).
     */
    public MetadataBuilder addExecuteBefore(Class<? extends RuleProvider> type)
    {
        if (type != null)
        {
            executeBefore.add(type);
        }
        return this;
    }

    @Override
    public List<String> getExecuteBeforeIDs()
    {
        return Collections.unmodifiableList(executeBeforeIDs);
    }

    /**
     * Set the list of the {@link RuleProvider} classes that should execute after the {@link Rule} instances in the corresponding {@link RuleProvider}
     * instance.
     *
     * <p>
     * This is returned as a list of Rule IDs in order to support extensions that cannot depend on each other via class names. For example, in the
     * case of the Groovy rules extension, a single class covers many rules with their own IDs.
     *
     * For specifying Java-based rules, {@link #getExecuteBefore()} is preferred.
     */
    public MetadataBuilder setExecuteBeforeIDs(List<String> executeBeforeIDs)
    {
        this.executeBeforeIDs = new ArrayList<>(executeBeforeIDs);
        return this;
    }

    /**
     * Add to the list of the {@link RuleProvider} classes that should execute after the {@link Rule} instances in the corresponding
     * {@link RuleProvider} instance.
     *
     * <p>
     * This is returned as a list of Rule IDs in order to support extensions that cannot depend on each other via class names. For example, in the
     * case of the Groovy rules extension, a single class covers many rules with their own IDs.
     *
     * For specifying Java-based rules, {@link #getExecuteBefore()} is preferred.
     */
    public MetadataBuilder addExecuteBeforeId(String id)
    {
        if (id != null)
        {
            executeBeforeIDs.add(id);
        }
        return this;
    }

    /**
     * Add to the {@link Set} of tags by which this {@link RulesetMetadata} is classified.
     * <p>
     * Inherits from {@link RulesetMetadata#getTags()} if available.
     */
    public MetadataBuilder addTags(String tag, String... tags)
    {
        if (!StringUtils.isBlank(tag))
            this.tags.add(tag.trim());

        if (tags != null)
        {
            for (String t : tags)
            {
                if (!StringUtils.isBlank(t))
                    this.tags.add(t.trim());
            }
        }

        return this;
    }

    public MetadataBuilder addTag(String tag)
    {
        if (!StringUtils.isBlank(tag))
        {
            this.tags.add(tag.trim());
        }
        return this;
    }

    @Override
    public Set<String> getTags()
    {
        return join(this.tags, parent.getTags(), super.getTags());
    }

    /**
     * Set the tags by which this {@link RulesetMetadata} is classified.
     * <p>
     * Inherits from {@link RulesetMetadata#getTags()} if available.
     */
    public MetadataBuilder setTags(List<String> tags)
    {
        if (tags == null)
            this.tags = new HashSet<>();
        else
            this.tags = Collections.unmodifiableSet(new HashSet<>(tags));

        return this;
    }

    @Override
    public Set<TechnologyReference> getSourceTechnologies()
    {
        return join(sourceTechnologies, super.getSourceTechnologies(), parent.getSourceTechnologies());
    }

    /**
     * Add to the {@link Set} of source {@link TechnologyReference} instances to which this {@link RuleProvider} is related.
     * <p>
     * Inherits from {@link RulesetMetadata#getSourceTechnologies()} if available.
     */
    public MetadataBuilder addSourceTechnology(TechnologyReference reference)
    {
        if (reference != null)
            sourceTechnologies.add(reference);

        return this;
    }

    @Override
    public Set<TechnologyReference> getTargetTechnologies()
    {
        return join(targetTechnologies, super.getTargetTechnologies(), parent.getTargetTechnologies());
    }

    /**
     * Add to the {@link Set} of target {@link TechnologyReference} instances to which this {@link RuleProvider} is related.
     * <p>
     * Inherits from {@link RulesetMetadata#getTargetTechnologies()} if available.
     */
    public MetadataBuilder addTargetTechnology(TechnologyReference reference)
    {
        if (reference != null)
            targetTechnologies.add(reference);

        return this;
    }

    @Override
    public Set<AddonId> getRequiredAddons()
    {
        return join(requiredAddons, super.getRequiredAddons(), parent.getRequiredAddons());
    }

    /**
     * Add to the {@link Set} of {@link Addon}s required to run this rule-set. (<b>Note:</b> This is typically only used in situations where rules are
     * provided externally - such as XML - whereas in Java, the {@link Addon} will already define its dependencies on other addons directly.)
     * <p>
     * Inherits from {@link RulesetMetadata#getRequiredAddons()} if available.
     */
    public MetadataBuilder addRequiredAddon(AddonId reference)
    {
        if (reference != null)
            requiredAddons.add(reference);

        return this;
    }

    /**
     * Whether Windup should stop execution if this provider's rule execution ends with an exception.
     *
     * By default, the exceptions are only logged and the failing rule appears in report. The rule itself is responsible for handling exceptions and
     * storing them into the graph.
     */
    public MetadataBuilder setHaltOnException(boolean haltOnException)
    {
        this.haltOnException = haltOnException;
        return this;
    }

    @Override
    public boolean isHaltOnException()
    {
        return haltOnException;
    }

    @Override
    public boolean isDisabled()
    {
        return disabled;
    }

    /**
     * Join N sets.
     */
    @SafeVarargs
    private final <T> Set<T> join(Set<T>... sets)
    {
        Set<T> result = new HashSet<>();
        if (sets == null)
            return result;

        for (Set<T> set : sets)
        {
            if (set != null)
                result.addAll(set);
        }
        return result;
    }
}
