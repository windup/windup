package org.jboss.windup.tooling;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

import javax.inject.Inject;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ToolingRMIServer
{
    @Inject
    private ExecutionBuilder executionBuilder;

    public void startServer(int port)
    {

        System.out.println("Registering RMI Server...");
        try
        {
            Registry registry = LocateRegistry.getRegistry(port);
            try
            {
                String[] registered = registry.list();
                if (Arrays.asList(registered).contains(ExecutionBuilder.LOOKUP_NAME))
                    registry.unbind(ExecutionBuilder.LOOKUP_NAME);

                try
                {
                    UnicastRemoteObject.unexportObject(executionBuilder, true);
                }
                catch (Throwable t)
                {
                    System.out.println("Could not unexport due to: " + t.getMessage());
                }
            }
            catch (Throwable t)
            {
                t.printStackTrace();
                System.out.println("Registry not already there, starting...");
                registry = LocateRegistry.createRegistry(port);
            }

            ExecutionBuilder proxy = (ExecutionBuilder) UnicastRemoteObject.exportObject(executionBuilder, 0);
            registry.rebind(ExecutionBuilder.LOOKUP_NAME, proxy);
            System.out.println("Registered ExecutionBuilder at: " + registry);
        }
        catch (RemoteException e)
        {
            System.out.println("Bootstrap error while registering ExecutionBuilder...");
            e.printStackTrace();
        }
    }
}