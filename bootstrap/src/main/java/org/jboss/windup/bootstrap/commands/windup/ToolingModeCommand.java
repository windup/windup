package org.jboss.windup.bootstrap.commands.windup;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.google.common.collect.Lists;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.forge.furnace.util.Sets;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.addons.AddImmutableAddonDirectoryCommand;
import org.jboss.windup.tooling.IOptionKeys;
import org.jboss.windup.tooling.ToolingModeRunner;
import org.jboss.windup.util.Theme;
import org.jboss.windup.util.ThemeProvider;

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
        int index = arguments.indexOf("--immutableAddonDir");
        if (index == -1) {
            return "";
        }
        return arguments.get(++index);
    }

    @Override
    public CommandResult execute()
    {
        Theme theme = ThemeProvider.getInstance().getTheme();

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
                System.out.println("Failed to start " + theme.getBrandNameAcronym() + "!");
                e.printStackTrace();
            }
            this.analyze();
            this.furnace.stop();
        }
        catch (Throwable t)
        {
            System.err.println(theme.getBrandNameAcronym() + " execution failed due to: " + t.getMessage());
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

        List<String> packages = this.getPackages();
        List<String> excludePackage = this.getExcludePackages();
        Map<String, Object> options = this.collectOptions();

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
        furnace.getAddonRegistry().getServices(ToolingModeRunner.class).get().run(input, output, sourceMode,
                ignoreReport, ignorePatterns, windupHome, source, target, rulesDir, packages, excludePackage, options);
    }

    public Map<String, Object> collectOptions()
    {
        Map<String, Object> options = new HashMap<String, Object>();

        // userIgnorePath
        String userIgnorePath = this.getUserIgnorePath();
        if (userIgnorePath != null) {
            options.put(IOptionKeys.USER_IGNORE_PATH, userIgnorePath);
        }

        // overwrite
        options.put(IOptionKeys.OVERWRITE, this.overwrite());

        // excludePackages - skip, special case

        // mavenizeGroupId
        String mavenizeGroupId = this.getMavenizeGroupId();
        if (mavenizeGroupId != null) {
            options.put(IOptionKeys.MAVENIZE_GROUP_ID, mavenizeGroupId);
        }

        // exportCSV
        options.put(IOptionKeys.EXPORT_CSV, this.exportCSV());

        // excludeTags
        List<String> excludeTags = this.getExcludeTags();
        if (!excludeTags.isEmpty()) {
            options.put(IOptionKeys.EXCLUDE_TAGS, excludeTags);
        }

        // packages - skip, special case

        // additionalClasspath
        List<File> additionalClasspath = this.getAdditionalClasspath();
        if (!additionalClasspath.isEmpty()) {
            options.put(IOptionKeys.ADDITIONAL_CLASSPATH, additionalClasspath);
        }

        // disableTattletale
        options.put(IOptionKeys.DISABLE_TATTLETALE, this.disableTattletale());

        // enableCompatibleFilesReport
        options.put(IOptionKeys.ENABLE_COMPATIBLE_FILES_REPORT, this.enableCompatibleFilesReport());

        // includeTags
        List<String> includeTags = this.getIncludeTags();
        if (!includeTags.isEmpty()) {
            options.put(IOptionKeys.INCLUDE_TAGS, includeTags);
        }

        // online
        options.put(IOptionKeys.ONLINE, this.online());

        // enableClassNotFoundAnalysis
        options.put(IOptionKeys.ENABLE_CLASS_NOT_FOUND_ANALYSIS, this.enableClassNotFoundAnalysis());

        // enableTattletale
        options.put(IOptionKeys.ENABLE_TATTLETALE, this.enableTattletale());

        // explodedApp
        options.put(IOptionKeys.EXPLODED_APP, this.explodedApp());

        // keepWorkDirs
        options.put(IOptionKeys.KEEP_WORK_DIRS, this.keepWorkDirs());

        // mavenize
        options.put(IOptionKeys.MAVENIZE, this.mavenize());

        // inputApplicationName
        String inputApplicationName = this.getInputApplicationName();
        if (inputApplicationName != null) {
            options.put(IOptionKeys.INPUT_APPLICATION_NAME, inputApplicationName);
        }

        return options;
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

    public Set<String> getInput()
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

    // userIgnorePath
    private String getUserIgnorePath()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.USER_IGNORE_PATH)) + 1;
        List<String> values = this.getValues(index);
        return values.size() == 1 ? values.get(0) : null;
    }

    // overwrite
    private boolean overwrite()
    {
        return this.arguments.contains(toArg(IOptionKeys.OVERWRITE));
    }

    // excludePackages
    public List<String> getExcludePackages()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.EXCLUDE_PACKAGES)) + 1;
        return this.getValues(index);
    }

    // mavenizeGroupId
    private String getMavenizeGroupId()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.MAVENIZE_GROUP_ID)) + 1;
        List<String> values = this.getValues(index);
        return values.size() == 1 ? values.get(0) : null;
    }

    // exportCSV
    private boolean exportCSV()
    {
        return this.arguments.contains(toArg(IOptionKeys.EXPORT_CSV));
    }

    // excludeTags
    private List<String> getExcludeTags()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.EXCLUDE_TAGS)) + 1;
        return this.getValues(index);
    }

    // packages
    public List<String> getPackages()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.PACKAGES)) + 1;
        return this.getValues(index);
    }

    // additionalClasspath
    private List<File> getAdditionalClasspath()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.ADDITIONAL_CLASSPATH)) + 1;
        List<File> locations = Lists.newArrayList();
        List<String> values = this.getValues(index);
        values.forEach(value -> locations.add(new File(value)));
        return locations;
    }

    // disableTattletale
    private boolean disableTattletale()
    {
        return this.arguments.contains(toArg(IOptionKeys.DISABLE_TATTLETALE));
    }

    // enableCompatibleFilesReport
    private boolean enableCompatibleFilesReport()
    {
        return this.arguments.contains(toArg(IOptionKeys.ENABLE_COMPATIBLE_FILES_REPORT));
    }

    // includeTags
    private List<String> getIncludeTags()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.INCLUDE_TAGS)) + 1;
        return this.getValues(index);
    }

    // online
    private boolean online()
    {
        return this.arguments.contains(toArg(IOptionKeys.ONLINE));
    }

    // enableClassNotFoundAnalysis
    private boolean enableClassNotFoundAnalysis()
    {
        return this.arguments.contains(toArg(IOptionKeys.ENABLE_CLASS_NOT_FOUND_ANALYSIS));
    }

    // enableTattletale
    private boolean enableTattletale()
    {
        return this.arguments.contains(toArg(IOptionKeys.ENABLE_TATTLETALE));
    }

    // explodedApp
    private boolean explodedApp()
    {
        return this.arguments.contains(toArg(IOptionKeys.EXPLODED_APP));
    }

    // keepWorkDirs
    private boolean keepWorkDirs()
    {
        return this.arguments.contains(toArg(IOptionKeys.KEEP_WORK_DIRS));
    }

    // mavenize
    private boolean mavenize()
    {
        return this.arguments.contains(toArg(IOptionKeys.MAVENIZE));
    }

    // inputApplicationName
    private String getInputApplicationName()
    {
        int index = arguments.indexOf(toArg(IOptionKeys.INPUT_APPLICATION_NAME)) + 1;
        List<String> values = this.getValues(index);
        return values.size() == 1 ? values.get(0) : null;
    }

    private static String toArg(String name) {
        return "--" + name;
    }

    private List<String> getValues(int index) {
        List<String> values = Lists.newArrayList();
        if (index == 0) {
            return values;
        }
        while (index < this.arguments.size())
        {
            final String arg = this.arguments.get(index);
            if (arg.contains("--"))
            {
                break;
            }
            values.add(arg.replace("\"", ""));
            index++;
        }
        return values;
    }
}
