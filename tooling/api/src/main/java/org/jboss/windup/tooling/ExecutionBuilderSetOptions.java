package org.jboss.windup.tooling;

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
     * Execute windup.
     */
    ExecutionResults execute();
}
