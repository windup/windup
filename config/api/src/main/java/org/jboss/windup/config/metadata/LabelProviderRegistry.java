package org.jboss.windup.config.metadata;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.LabelProvider;

import java.util.*;

public class LabelProviderRegistry
{
    private final List<LabelProvider> providers = new ArrayList<>();
    private final IdentityHashMap<LabelProvider, List<Label>> providersToLabels = new IdentityHashMap<>();

    /**
     * Gets the current instance of {@link LabelProviderRegistry}.
     */
    public static LabelProviderRegistry instance(GraphRewrite event)
    {
        return (LabelProviderRegistry) event.getRewriteContext().get(LabelProviderRegistry.class);
    }

    /**
     * Sets the list of loaded {@link LabelProvider}s.
     */
    public void setProviders(List<LabelProvider> providers)
    {
        this.providers.clear();
        this.providers.addAll(providers);
    }

    /**
     * Gets the list of loaded {@link LabelProvider}s as an immutable {@link List}.
     */
    public List<LabelProvider> getProviders()
    {
        return Collections.unmodifiableList(providers);
    }

    /**
     * Sets the {@link List} of {@link Label}s that were loaded from the given {@link LabelProvider}.
     */
    public void setLabels(LabelProvider provider, List<Label> labels)
    {
        providersToLabels.put(provider, labels);
    }

    /**
     * Gets all of the {@link Label}s that were loaded by the given {@link LabelProvider}.
     */
    public List<Label> getLabels(LabelProvider provider)
    {
        List<Label> labels = providersToLabels.get(provider);
        if (labels == null)
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(labels);
    }

}
