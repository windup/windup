package org.jboss.windup.server;

/**
 * Provides an interface for bootstrap to use to find servers within the addons.
 * <p>
 * The name is the parameter that Bootstrap will look for on the CLI. If this is found, then any remaining parameters
 * will be passed directly to the server provider.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface WindupServerProvider {

    /**
     * Gets a name to be used from the CLI to indicate that this should start this service provider.
     */
    String getName();

    /**
     * Runs the server. This should block until the server has completed or been killed.
     * <p>
     * The definition of "completed" is up to the implementation of the service interface.
     */
    void runServer(String[] arguments);
}
