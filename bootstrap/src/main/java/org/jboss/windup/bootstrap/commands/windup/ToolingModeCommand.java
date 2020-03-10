package org.jboss.windup.bootstrap.commands.windup;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.forge.furnace.util.Sets;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.addons.AddImmutableAddonDirectoryCommand;
import org.jboss.windup.tooling.IOptionKeys;
import org.jboss.windup.tooling.ToolingModeRunner;
import org.jboss.windup.util.Util;

public class ToolingModeCommand implements Command
{
    public static final String COMMAND_ID = "--toolingMode";

    private Furnace furnace;

    private List<String> arguments;
    private String addonsDirectory;

    public ToolingModeCommand(List<String> arguments)
    {
        this.arguments = arguments;
        this.addonsDirectory = getAddonDirectory(arguments);
    }

    private static String getAddonDirectory(List<String> arguments)
    {
        int addDirectoryIndex = arguments.indexOf("--immutableAddonDir") + 1;
        return arguments.get(addDirectoryIndex);
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
                future.get();
            }
            catch (Exception e)
            {
                System.out.println("Failed to start "+Util.WINDUP_BRAND_NAME_ACRONYM+"!");
                e.printStackTrace();
            }
            this.analyze();
            this.furnace.stop();
        }
        catch (Throwable t)
        {
            System.err.println(Util.WINDUP_BRAND_NAME_ACRONYM + " execution failed due to: " + t.getMessage());
            t.printStackTrace();
        }
        return null;
    }

    private void loadAddons()
    {
        AddImmutableAddonDirectoryCommand addonCommand = new AddImmutableAddonDirectoryCommand(addonsDirectory);
        addonCommand.setFurnace(furnace);
        addonCommand.execute();
    }

    private void analyze()
    {
        System.out.println("Calling ToolingModeRunner...");
        Set<String> input = this.getInput();
        String output = this.getOutput();
        boolean sourceMode = this.isSourceMode();
        boolean ignoreReport = this.ignoreReport();
        List<String> ignorePatterns = this.getIgnorePatterns();
        String windupHome = this.getWindupHome();
        List<String> source = this.getSource();
        List<String> target = this.getTarget();
        List<File> rulesDir = this.getUserRulesDir();

        if (input.isEmpty()) {
            System.out.println("Error - `input` required");
            return;
        }

        if (output == null) {
            System.out.println("Error - `output` required");
            return;
        }

        if (windupHome == null) {
            System.out.println("Error - `windupHome` required");
            return;
        }

        if (target.isEmpty()) {
            System.out.println("Error - `target` required");
            return;
        }

        System.out.println("Using Data: ");
        System.out.println("input: " + input);
        System.out.println("output: " + output);
        System.out.println("sourceMode: " + sourceMode);
        System.out.println("skipReport: " + ignoreReport);
        System.out.println("ignorePatterns: " + ignorePatterns);
        System.out.println("windupHome: " + windupHome);
        System.out.println("source: " + source);
        System.out.println("target: " + target);
        System.out.println("userRulesDirectory: " + rulesDir);
        furnace.getAddonRegistry().getServices(ToolingModeRunner.class).get()
            .run(input, output, sourceMode, ignoreReport, ignorePatterns,
                    windupHome, source, target, rulesDir);
    }

    @Override
    public CommandPhase getPhase()
    {
        return null;
    }

    public static boolean isToolingMode(List<String> arguments)
    {
        return arguments.contains(ToolingModeCommand.COMMAND_ID);
    }

    private Set<String> getInput()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.INPUT)) + 1;
        return Sets.toSet(this.getValues(index));
    }

    private String getOutput()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.OUTPUT)) + 1;
        List<String> values = this.getValues(index);
        return values.size() == 1 ? values.get(0) : null;
    }

    private boolean isSourceMode()
    {
        return this.arguments.contains(toArg(IOptionKeys.SOURCE_MODE));
    }

    private boolean ignoreReport()
    {
        return this.arguments.contains(toArg(IOptionKeys.SKIP_REPORTS));
    }

    private List<String> getIgnorePatterns()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.IGNORE_PATTERN)) + 1;
        return this.getValues(index);
    }

    private String getWindupHome()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.HOME)) + 1;
        List<String> values = this.getValues(index);
        return values.size() == 1 ? values.get(0) : null;
    }

    private List<String> getSource()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.SOURCE)) + 1;
        return this.getValues(index);
    }

    private List<String> getTarget()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.TARGET)) + 1;
        return this.getValues(index);
    }

    private List<File> getUserRulesDir()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.CUSTOM_RULES_DIR)) + 1;
        List<File> rules = Lists.newArrayList();
        List<String> values = this.getValues(index);
        values.forEach(value -> rules.add(new File(value)));
        return rules;
    }

    private String toArg(String name) {
        return "--" + name;
    }

    private List<String> getValues(int index) {
        List<String> values = Lists.newArrayList();
        if (index == 0) {
            return values;
        }
        while (index < arguments.size())
        {
            final String arg = arguments.get(index);
            if (arg.contains("--"))
            {
                break;
            }
            for (String value : StringUtils.split(arg, ' '))
                values.add(value);
            index++;
        }
        return values;
    }
}
