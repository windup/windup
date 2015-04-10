package org.jboss.windup.tooling;

import org.jboss.windup.exec.WindupProgressMonitor;

/**
 * Allows setting windup options, including the {@link WindupProgressMonitor}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ExecutionBuilderSetOptionsAndProgressMonitor
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
     * Sets the callback that will be used for monitoring progress.
     */
    ExecutionBuilderSetOptions setProgressMonitor(WindupProgressMonitor monitor);

    /**
     * Execute windup.
     */
    ExecutionResults execute();
}
