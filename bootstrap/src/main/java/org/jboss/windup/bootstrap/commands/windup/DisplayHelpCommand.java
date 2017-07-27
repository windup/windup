package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.help.Help;
import org.jboss.windup.bootstrap.help.OptionDescription;
import org.jboss.windup.util.Util;

public class DisplayHelpCommand implements Command
{
    @Override
    public CommandResult execute()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: "+Util.WINDUP_CLI_NAME+" [OPTION]... PARAMETER ... \n");
        sb.append("Extendable migration analysis, at your fingertips. \n");
        sb.append("\n");

        sb.append("\n"+Util.WINDUP_BRAND_NAME_ACRONYM +" CLI Options:\n");

        for (OptionDescription option : Help.load().getOptions())
        {
            sb.append("--").append(option.getName()).append("\n");
            sb.append("\t").append(option.getDescription()).append("\n");
        }

        sb.append("--listTags\n");
        sb.append("\t List all available tags.\n");

        sb.append("--listSourceTechnologies\n");
        sb.append("\t List all available source technologies.\n");

        sb.append("--listTargetTechnologies\n");
        sb.append("\t List all available target technologies.\n");

        sb.append("--discoverPackages\n");
        sb.append("\t Lists all available packages in the input application (--input must also be specified).\n");

// temporary disabled until we find out how to properly update rules 
//        sb.append("--updateRulesets\n");
//        sb.append("\t Update the core rulesets to the latest version available.\n");

        sb.append("\nForge Options:\n");

        sb.append("-i, --install GROUP_ID:ARTIFACT_ID[:VERSION]\n");
        sb.append("\t install the required addons and exit. ex: `"+Util.WINDUP_CLI_NAME+" -i core-addon-x` or `"+Util.WINDUP_CLI_NAME+" -i org.example.addon:example:1.0.0` \n");

        sb.append("-r, --remove GROUP_ID:ARTIFACT_ID[:VERSION]\n");
        sb.append("\t remove the required addons and exit. ex: `"+Util.WINDUP_CLI_NAME+" -r core-addon-x` or `"+Util.WINDUP_CLI_NAME+" -r org.example.addon:example:1.0.0` \n");

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
        return CommandPhase.PRE_CONFIGURATION;
    }
}
