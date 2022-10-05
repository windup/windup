package org.jboss.windup.engine.predicates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.RuleProvider;

/**
 * Executes only those {@link RuleProvider} instances that were specified in this {@link Predicate}.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class EnumeratedRuleProviderPredicate implements Predicate<RuleProvider> {
    private final Set<Class<? extends RuleProvider>> enabledProviders = new HashSet<>();

    @SafeVarargs
    public EnumeratedRuleProviderPredicate(Class<? extends RuleProvider> provider, Class<? extends RuleProvider>... providers) {
        if (provider != null)
            this.enabledProviders.add(provider);

        this.enabledProviders.addAll(Arrays.asList(providers));
    }

    @Override
    public boolean accept(RuleProvider provider) {
        Class<? extends RuleProvider> clazz = provider.getClass();
        for (Class<? extends RuleProvider> enabledClazz : enabledProviders) {
            if (enabledClazz.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
}