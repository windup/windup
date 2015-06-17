package org.jboss.windup.tooling;

import org.jboss.windup.config.ConfigurationOption;

/**
 * Allows configuring options on Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ExecutionBuilderSetOptions
{
    /**
     * Sets a pattern of file paths to ignore during processing.
     */
    ExecutionBuilderSetOptions ignore(String ignorePattern);

    /**
     * Sets the package name prefixes to scan (the default is to scan all packages).
     */
    ExecutionBuilderSetOptions includePackage(String includePackagePrefix);

    /**
     * Sets the package name prefixes to ignore.
     */
    ExecutionBuilderSetOptions excludePackage(String excludePackagePrefix);

    /**
     * Sets the option with the specified name to the specified value. Option names can be found in static variables on {@link ConfigurationOption}
     * implementations.
     */
    ExecutionBuilderSetOptions setOption(String name, Object value);

    /**
     * Execute windup.
     */
    ExecutionResults execute();
}
