package org.jboss.windup.config.loader;

import org.jboss.windup.config.LabelProvider;

import java.util.List;

/**
 * Defines a way Windup can load {@link LabelProvider}s. We might have multiple ways of loading a {@link LabelProvider}s for instance might have
 * XMLLabelProviderLoader, JSONLabelproviderLoader, JavaLabelProviderLoader, etc.
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public interface LabelProviderLoader
{
    /**
     * Indicates if labels are loaded from the filesystem.
     */
    boolean isFileBased();

    /**
     * Return all {@link LabelProvider} instances that are relevant for this loader.
     */
    List<LabelProvider> getProviders(RuleLoaderContext ruleLoaderContext);
}
