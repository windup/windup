package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.exec.configuration.WindupConfiguration;

public class DisplayHelpCommand implements Command, FurnaceDependent
{
    private Furnace furnace;

    @Override
    public void setFurnace(Furnace furnace)
    {
        this.furnace = furnace;
    }

    @Override
    public CommandResult execute()
    {
        Iterable<ConfigurationOption> windupOptions = WindupConfiguration.getWindupConfigurationOptions(furnace);

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

        sb.append("--listTags\n");
        sb.append("\t List all available tags\n");

        sb.append("--listSourceTechnologies\n");
        sb.append("\t List all available source technologies\n");

        sb.append("--listTargetTechnologies\n");
        sb.append("\t List all available target technologies\n");

        sb.append("--updateRulesets\n");
        sb.append("\t Update the core rulesets to the latest version available\n");

        sb.append("\nForge Options:\n");

        sb.append("-i, --install GROUP_ID:ARTIFACT_ID[:VERSION]\n");
        sb.append("\t install the required addons and exit. ex: `windup -i core-addon-x` or `windup -i org.example.addon:example,1.0.0` \n");

        sb.append("-r, --remove GROUP_ID:ARTIFACT_ID[:VERSION]\n");
        sb.append("\t remove the required addons and exit. ex: `windup -r core-addon-x` or `windup -r org.example.addon:example,1.0.0` \n");

        sb.append("-l, --list\n");
        sb.append("\t list installed addons and exit \n");

        sb.append("-a, --addonDir DIR\n");
        sb.append("\t add the given directory for use as a custom addon repository \n");

        sb.append("-m, --immutableAddonDir DIR\n");
        sb.append("\t add the given directory for use as a custom immutable addon repository (read only) \n");

        sb.append("-b, --batchMode\n");
        sb.append("\t run Forge in batch mode and does not prompt for confirmation (exits immediately after running) \n");

        sb.append("-d, --debug\n");
        sb.append("\t run Forge in debug mode (wait on port 8000 for a debugger to attach) \n");

        sb.append("-h, --help\n");
        sb.append("\t display this help and exit \n");

        sb.append("-v, --version\n");
        sb.append("\t output version information and exit \n");

        System.out.println(sb.toString());
        return CommandResult.EXIT;
    }

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.PRE_EXECUTION;
    }
}
