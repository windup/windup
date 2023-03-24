package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.help.Help;
import org.jboss.windup.bootstrap.help.OptionDescription;
import org.jboss.windup.util.Theme;
import org.jboss.windup.util.ThemeProvider;

public class DisplayHelpCommand implements Command {
    @Override
    public CommandResult execute() {
        Theme theme = ThemeProvider.getInstance().getTheme();

        StringBuilder sb = new StringBuilder();
        sb.append("Usage: windup-cli [OPTION]... PARAMETER ... ").append(System.lineSeparator());
        sb.append("Extendable migration analysis, at your fingertips.  ").append(System.lineSeparator());
        sb.append(System.lineSeparator());

        sb.append(System.lineSeparator()).append(theme.getBrandNameAcronym() + " CLI Options:").append(System.lineSeparator());

        for (OptionDescription option : Help.load().getOptions()) {
            sb.append("--").append(option.getName()).append(System.lineSeparator());
            sb.append("\t");
            sb.append(option.isRequired() ? "(Required) " : "");
            sb.append(option.getDescription()).append(System.lineSeparator());
        }

        sb.append("--listTags").append(System.lineSeparator());
        sb.append("\t List all available tags.").append(System.lineSeparator());

        sb.append("--listSourceTechnologies").append(System.lineSeparator());
        sb.append("\t List all available source technologies.").append(System.lineSeparator());

        sb.append("--listTargetTechnologies").append(System.lineSeparator());
        sb.append("\t List all available target technologies.").append(System.lineSeparator());

        sb.append("--discoverPackages").append(System.lineSeparator());
        sb.append("\t Lists all available packages in the input application (--input must also be specified).").append(System.lineSeparator());

// temporary disabled until we find out how to properly update rules 
//        sb.append("--updateRulesets").append(System.lineSeparator());
//        sb.append("\t Update the core rulesets to the latest version available.").append(System.lineSeparator());

        sb.append(System.lineSeparator()).append(" Forge Options:").append(System.lineSeparator());

        sb.append("-b, --batchMode").append(System.lineSeparator());
        sb.append("\t run Forge in batch mode and does not prompt for confirmation (exits immediately after running) ").append(System.lineSeparator());

        sb.append("-e, --exitCodes").append(System.lineSeparator());
        sb.append("\t when batch mode is enabled (check above) the process exits providing exit codes ").append(System.lineSeparator());

        sb.append("-i, --install GROUP_ID:ARTIFACT_ID[:VERSION]").append(System.lineSeparator());
        sb.append("\t install the required addons and exit. ex: `" + theme.getCliName() + " -i core-addon-x` or `" + theme.getCliName() + " -i org.example.addon:example:1.0.0` ").append(System.lineSeparator());

        sb.append("-r, --remove GROUP_ID:ARTIFACT_ID[:VERSION]").append(System.lineSeparator());
        sb.append("\t remove the required addons and exit. ex: `" + theme.getCliName() + " -r core-addon-x` or `" + theme.getCliName() + " -r org.example.addon:example:1.0.0` ").append(System.lineSeparator());

        sb.append("-l, --list").append(System.lineSeparator());
        sb.append("\t list installed addons and exit ").append(System.lineSeparator());

        sb.append("-a, --addonDir DIR").append(System.lineSeparator());
        sb.append("\t add the given directory for use as a custom addon repository ").append(System.lineSeparator());

        sb.append("-m, --immutableAddonDir DIR").append(System.lineSeparator());
        sb.append("\t add the given directory for use as a custom immutable addon repository (read only) ").append(System.lineSeparator());

        sb.append("-d, --debug").append(System.lineSeparator());
        sb.append("\t run Forge in debug mode (wait on port 8000 for a debugger to attach) ").append(System.lineSeparator());

        sb.append("-h, --help").append(System.lineSeparator());
        sb.append("\t display this help and exit ").append(System.lineSeparator());

        sb.append("-v, --version").append(System.lineSeparator());
        sb.append("\t output version information and exit ").append(System.lineSeparator());

        System.out.println(sb.toString());
        return CommandResult.EXIT;
    }

    @Override
    public CommandPhase getPhase() {
        return CommandPhase.PRE_CONFIGURATION;
    }
}
