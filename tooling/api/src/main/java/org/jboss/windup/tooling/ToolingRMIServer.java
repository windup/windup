package org.jboss.windup.tooling;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.inject.Inject;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ToolingRMIServer {
    private static Logger LOG = Logger.getLogger(ToolingRMIServer.class.getName());

    @Inject
    private ExecutionBuilder executionBuilder;

    public void startServer(int port, String version) {
        LOG.info("Registering RMI Server...");
        try {
            executionBuilder.setVersion(version);
            Registry registry = LocateRegistry.getRegistry(port);
            try {
                String[] registered = registry.list();
                if (Arrays.asList(registered).contains(ExecutionBuilder.LOOKUP_NAME))
                    registry.unbind(ExecutionBuilder.LOOKUP_NAME);

                try {
                    UnicastRemoteObject.unexportObject(executionBuilder, true);
                } catch (Throwable t) {
                    LOG.warning("Could not unexport due to: " + t.getMessage());
                }
            } catch (Throwable t) {
                LOG.warning("Registry not already there, starting...");
                registry = LocateRegistry.createRegistry(port);
            }

            ExecutionBuilder proxy = (ExecutionBuilder) UnicastRemoteObject.exportObject(executionBuilder, 0);
            registry.rebind(ExecutionBuilder.LOOKUP_NAME, proxy);

            LOG.info("Registered ExecutionBuilder at: " + registry);
        } catch (RemoteException e) {
            LOG.severe("Bootstrap error while registering ExecutionBuilder...");
            e.printStackTrace();
        }
    }
}
