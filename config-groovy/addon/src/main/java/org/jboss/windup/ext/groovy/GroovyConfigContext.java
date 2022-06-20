package org.jboss.windup.ext.groovy;

import org.jboss.windup.config.loader.RuleLoaderContext;

/**
 * Context for creating {@link GroovyConfigMethod} closure callbacks.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface GroovyConfigContext {
    /**
     * Get the {@link RuleLoaderContext}
     */
    RuleLoaderContext getRuleLoaderContext();
}
