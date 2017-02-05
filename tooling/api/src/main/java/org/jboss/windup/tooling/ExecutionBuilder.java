package org.jboss.windup.tooling;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import org.jboss.windup.tooling.rules.RuleProviderRegistry;

/**
 * The initial call, specifying the installation path to Windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ExecutionBuilder extends Remote
{
    String LOOKUP_NAME = "ExecutionBuilder";
    
    /**
     * Start building a Windup execution with a windup that is installed at the specified path.
     */
    void setWindupHome(String windupHome) throws RemoteException;

    /**
     * Sets the callback that will be used for monitoring progress.
     */
    void setProgressMonitor(WindupToolingProgressMonitor monitor) throws RemoteException;

    /**
     * Sets the input path (application source directory, or application binary file).
     */
    void setInput(String input) throws RemoteException;

    /**
     * Sets a pattern of file paths to ignore during processing.
     */
    void ignore(String ignorePattern) throws RemoteException;

    /**
     * Sets the package name prefixes to scan (the default is to scan all packages).
     */
    void includePackage(String includePackagePrefix) throws RemoteException;

    /**
     * Sets the package name prefixes to ignore.
     */
    void excludePackage(String excludePackagePrefix) throws RemoteException;

    /**
     * Sets the option with the specified name to the specified value. Option names can be found in static variables on {@link ConfigurationOption}
     * implementations.
     */
    void setOption(String name, Object value) throws RemoteException;

    /**
     * Execute windup.
     */
    ExecutionResults execute() throws RemoteException;

    /**
     * Sets the output path for Windup (where the graph will be stored, and where the reports will be generated).
     */
    void setOutput(String output) throws RemoteException;

    /**
     * Includes the provided list of package prefixes.
     */
    void includePackages(Collection<String> includePackagePrefixes) throws RemoteException;

    /**
     * Sets a list of package name prefixes to ignore.
     */
    void excludePackages(Collection<String> excludePackagePrefixes) throws RemoteException;

    /**
     * Switches the engine to run in source only mode (no decompilation).
     */
    void sourceOnlyMode() throws RemoteException;

    /**
     * Indicates that Windup should not generate reports at the end.
     */
    void skipReportGeneration() throws RemoteException;

    /**
     * Adds a custom uer rules path.
     */
    void addUserRulesPath(String rulesPath) throws RemoteException;

    /**
     * Adds a set of custom uer rules paths.
     */
    void addUserRulesPaths(Iterable<String> rulesPath) throws RemoteException;

    /**
     * Clears the configuration information.
     */
    void clear() throws RemoteException;

    /**
     * Terminates the runtime that registered this ExecutionBuilder.
     */
    void terminate() throws RemoteException;
    
    /**
     * Returns the Windup version.
     */
    String getVersion() throws RemoteException;
    
    /**
     * Sets the version of Windup.
     */
    void setVersion(String version) throws RemoteException;
    
    /**
     * Returns the registry containing the rule providers.
     */
    RuleProviderRegistry getRuleProviderRegistry() throws RemoteException;
}
