package org.jboss.windup.tooling;

import org.jboss.windup.config.ConfigurationOption;

import java.nio.file.Path;
import java.util.Collection;

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
     * Includes the provided list of package prefixes.
     */
    ExecutionBuilderSetOptions includePackages(Collection<String> includePackagePrefixes);

    /**
     * Sets the package name prefixes to ignore.
     */
    ExecutionBuilderSetOptions excludePackage(String excludePackagePrefix);

    /**
     * Sets a list of package name prefixes to ignore.
     */
    ExecutionBuilderSetOptions excludePackages(Collection<String> excludePackagePrefixes);

    /**
     * Switches the engine to run in source only mode (no decompilation).
     */
    ExecutionBuilderSetOptions sourceOnlyMode();

    /**
     * Indicates that Windup should not generate reports at the end.
     */
    ExecutionBuilderSetOptions skipReportGeneration();

    /**
     * Adds a custom uer rules path.
     */
    ExecutionBuilderSetOptions addUserRulesPath(Path rulesPath);

    /**
     * Adds a set of custom uer rules paths.
     */
    ExecutionBuilderSetOptions addUserRulesPaths(Iterable<Path> rulesPath);

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
