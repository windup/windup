package org.jboss.windup.bootstrap.listener;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.exec.configuration.WindupConfiguration;

public class DisplayHelpThread extends Thread
{

    private Furnace furnace;

    public DisplayHelpThread(Furnace furnace)
    {
        this.furnace = furnace;
    }

    @Override
    public void run() {
            try
            {
                waitUntilStable(furnace);
                Iterable<ConfigurationOption> knownWindupArgs = getKnownWindupArgs(furnace);
                System.out.println(getHelpMessage(knownWindupArgs));
                furnace.stop();
            }
            catch (InterruptedException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
    }

    private void waitUntilStable(Furnace furnace) throws InterruptedException
    {
        while (furnace.getStatus().isStarting())
        {
            Thread.sleep(10);
        }
    }

    private static Iterable<ConfigurationOption> getKnownWindupArgs(Furnace furnace)
    {
        return WindupConfiguration.getWindupConfigurationOptions(furnace);
    }

    private static String getHelpMessage(Iterable<ConfigurationOption> windupOptions)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: windup [OPTION]... PARAMETER ... \n");
        sb.append("Extendable migration analysis, at your fingertips. \n");
        sb.append("\n");

        sb.append("\nWindup Options:\n");
        for (ConfigurationOption option : windupOptions)
        {
            sb.append("--").append(option.getName()).append("\n");
            sb.append("\t").append(option.getDescription()).append("\n");
        }

        sb.append("\nForge Options:\n");

        sb.append("-i, --install [[groupId:]addon[,version]]\n");
        sb.append("\t install the required addons and exit. ex: `windup -i core-addon-x` or `windup -i org.example.addon:example,1.0.0` \n");

        sb.append("-r, --remove [[groupId:]addon[,version]]\n");
        sb.append("\t remove the required addons and exit. ex: `windup -r core-addon-x` or `windup -r org.example.addon:example,1.0.0` \n");

        sb.append("-l, --list\n");
        sb.append("\t list installed addons and exit \n");

        sb.append("-a, --addonDir [dir]\n");
        sb.append("\t add the given directory for use as a custom addon repository \n");

        sb.append("-e, --evaluate [cmd]\n");
        sb.append("\t evaluate the given string as commands (requires shell addon. Install via: `windup -i shell`) \n");

        sb.append("-m, --immutableAddonDir [dir]\n");
        sb.append("\t add the given directory for use as a custom immutable addon repository (read only) \n");

        sb.append("-b, --batchMode\n");
        sb.append("\t run Forge in batch mode and does not prompt for confirmation (exits immediately after running) \n");

        sb.append("-d, --debug\n");
        sb.append("\t run Forge in debug mode (wait on port 8000 for a debugger to attach) \n");

        sb.append("-h, --help\n");
        sb.append("\t display this help and exit \n");

        sb.append("-v, --version\n");
        sb.append("\t output version information and exit \n");
        return sb.toString();
    }

}
