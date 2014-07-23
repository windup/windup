package org.jboss.windup.ext.groovy;

import groovy.lang.Closure;

/**
 * Used to extend the groovy "simple" rule syntax with additional methods.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface GroovyConfigMethod
{
    /**
     * Return the name of the configuration method.
     */
    String getName(GroovyConfigContext context);

    /**
     * Return the {@link Closure} to be executed upon invocation.
     */
    Closure<?> getClosure(GroovyConfigContext context);
}
