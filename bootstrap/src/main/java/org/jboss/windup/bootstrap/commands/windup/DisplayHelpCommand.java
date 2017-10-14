package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.help.Help;
import org.jboss.windup.bootstrap.help.OptionDescription;
import org.jboss.windup.util.Util;
import static org.jboss.windup.util.Util.NL;

public class DisplayHelpCommand implements Command
{
    @Override
    public CommandResult execute()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: "+Util.WINDUP_CLI_NAME+" [OPTION]... PARAMETER ... ").append(NL);
        sb.append("Extendable migration analysis, at your fingertips.  ").append(NL);
        sb.append(NL);

        sb.append(NL).append(Util.WINDUP_BRAND_NAME_ACRONYM +" CLI Options:").append(NL);

        for (OptionDescription option : Help.load().getOptions())
        {
            sb.append("--").append(option.getName()).append(NL);
            sb.append("\t").append(option.getDescription()).append(NL);
        }

        sb.append("--listTags").append(NL);
        sb.append("\t List all available tags.").append(NL);

        sb.append("--listSourceTechnologies").append(NL);
        sb.append("\t List all available source technologies.").append(NL);

        sb.append("--listTargetTechnologies").append(NL);
        sb.append("\t List all available target technologies.").append(NL);

        sb.append("--discoverPackages").append(NL);
        sb.append("\t Lists all available packages in the input application (--input must also be specified).").append(NL);

// temporary disabled until we find out how to properly update rules
//        sb.append("--updateRulesets").append(NL);
//        sb.append("\t Update the core rulesets to the latest version available.").append(NL);

        sb.append(NL).append(" Forge Options:").append(NL);

        sb.append("-i, --install GROUP_ID:ARTIFACT_ID[:VERSION]").append(NL);
        sb.append("\t install the required addons and exit. ex: `"+Util.WINDUP_CLI_NAME+" -i core-addon-x` or `"+Util.WINDUP_CLI_NAME+" -i org.example.addon:example:1.0.0` ").append(NL);

        sb.append("-r, --remove GROUP_ID:ARTIFACT_ID[:VERSION]").append(NL);
        sb.append("\t remove the required addons and exit. ex: `"+Util.WINDUP_CLI_NAME+" -r core-addon-x` or `"+Util.WINDUP_CLI_NAME+" -r org.example.addon:example:1.0.0` ").append(NL);

        sb.append("-l, --list").append(NL);
        sb.append("\t list installed addons and exit ").append(NL);

        sb.append("-a, --addonDir DIR").append(NL);
        sb.append("\t add the given directory for use as a custom addon repository ").append(NL);

        sb.append("-m, --immutableAddonDir DIR").append(NL);
        sb.append("\t add the given directory for use as a custom immutable addon repository (read only) ").append(NL);

        sb.append("-b, --batchMode").append(NL);
        sb.append("\t run Forge in batch mode and does not prompt for confirmation (exits immediately after running) ").append(NL);

        sb.append("-d, --debug").append(NL);
        sb.append("\t run Forge in debug mode (wait on port 8000 for a debugger to attach) ").append(NL);

        sb.append("-h, --help").append(NL);
        sb.append("\t display this help and exit ").append(NL);

        sb.append("-v, --version").append(NL);
        sb.append("\t output version information and exit ").append(NL);

        System.out.println(sb.toString());
        return CommandResult.EXIT;
    }

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.PRE_CONFIGURATION;
    }
}
