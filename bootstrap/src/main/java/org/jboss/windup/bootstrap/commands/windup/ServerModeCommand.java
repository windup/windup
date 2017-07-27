package org.jboss.windup.bootstrap.commands.windup;

import java.util.List;
import java.util.concurrent.Future;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.addons.AddImmutableAddonDirectoryCommand;
import org.jboss.windup.tooling.ToolingRMIServer;
import org.jboss.windup.util.Util;

public class ServerModeCommand implements Command
{
    public static final String COMMAND_ID = "--startServer";

    private Furnace furnace;

    private int port;
    private String addonsDirectory;

    public ServerModeCommand(List<String> arguments)
    {
        this.port = getServerPort(arguments);
        this.addonsDirectory = getAddonDirectory(arguments);
    }

    public static boolean isServerMode(List<String> arguments)
    {
        return arguments.contains(ServerModeCommand.COMMAND_ID);
    }

    private static String getAddonDirectory(List<String> arguments)
    {
        int addDirectoryIndex = arguments.indexOf("--immutableAddonDir") + 1;
        return arguments.get(addDirectoryIndex);
    }

    private static int getServerPort(List<String> arguments)
    {
        int serverPort = arguments.indexOf(ServerModeCommand.COMMAND_ID) + 1;
        String serverPortString = arguments.get(serverPort);
        return Integer.valueOf(serverPortString);
    }

    @Override
    public CommandResult execute()
    {
        try
        {
            furnace = FurnaceFactory.getInstance();
            furnace.setServerMode(true);

            loadAddons();

            try
            {
                Future<Furnace> future = furnace.startAsync();
                future.get(); // use future.get() to wait until it is started
            }
            catch (Exception e)
            {
                System.out.println("Failed to start "+Util.WINDUP_BRAND_NAME_ACRONYM+"!");
                if (e.getMessage() != null)
                    System.out.println("Failure reason: " + e.getMessage());
                e.printStackTrace();
            }

            startServer();
        }
        catch (Throwable t)
        {
            System.err.println(Util.WINDUP_BRAND_NAME_ACRONYM+" execution failed due to: " + t.getMessage());
            t.printStackTrace();
        }

        System.out.println("Server started...");
        return null;
    }

    private void loadAddons()
    {
        AddImmutableAddonDirectoryCommand addonCommand = new AddImmutableAddonDirectoryCommand(addonsDirectory);
        addonCommand.setFurnace(furnace);
        addonCommand.execute();
    }

    private void startServer()
    {
        System.out.println("Calling ToolingRMIServer start...");
        furnace.getAddonRegistry().getServices(ToolingRMIServer.class).get().startServer(port, Bootstrap.getVersion());
    }

    // TODO: Not sure if this is necessary, or if killing the processes is sufficient.
    // If necessary, the client needs to invoke.
    @SuppressWarnings("unused")
    private void stop()
    {
        if (furnace != null && !furnace.getStatus().isStopped())
            furnace.stop();
    }

    @Override
    public CommandPhase getPhase()
    {
        return null;
    }
}
