package org.jboss.windup.config.metadata;

import org.jboss.windup.config.loader.RuleLoaderContext;
import java.util.Collection;

/**
 * Provides a mechanism for loading {@link TechnologyReferenceAliasTranslator} instances. Sublcasses will provide specific
 * implementations.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface TechnologyReferenceAliasTranslatorLoader
{
    Collection<TechnologyReferenceAliasTranslator> loadTranslators(RuleLoaderContext ruleLoaderContext);
}
