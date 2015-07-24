/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.windup.bootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.impl.addons.AddonRepositoryImpl;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.repositories.MutableAddonRepository;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.furnace.versions.EmptyVersion;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.exec.configuration.options.OutputPathOption;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.exec.updater.RulesetsUpdater;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.util.PathUtil;

/**
 * A class with a main method to bootstrap Windup.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Bootstrap
{
    private static final long MAX_COMPLETION_AGE = 10000L * 60L * 60L * 24L;

    private BootstrapFurnaceService furnaceService = null;
    private boolean batchMode = false;

    public static void main(final String[] args)
    {
        final List<String> bootstrapArgs = new ArrayList<>();

        for (String arg : args)
        {
            if (!handleAsSystemProperty(arg))
                bootstrapArgs.add(arg);
        }

        File rulesDir = new File(getUserWindupDir(), "rules");
        if (!rulesDir.exists())
        {
            rulesDir.mkdirs();
        }

        final String defaultLog = new File(getUserWindupDir(), "log/windup.log").getAbsolutePath();
        final String logDir = System.getProperty("org.jboss.forge.log.file", defaultLog);

        System.setProperty("org.jboss.forge.log.file", logDir);

        final String logManagerName = getServiceName(Bootstrap.class.getClassLoader(), "java.util.logging.LogManager");
        if (logManagerName != null)
        {
            System.setProperty("java.util.logging.manager", logManagerName);
        }

        new Bootstrap().run(bootstrapArgs);
    }

    private void run(List<String> args)
    {
        try
        {
            Furnace furnace = FurnaceFactory.getInstance();
            furnaceService = new BootstrapFurnaceService(furnace);

            processArguments(args, furnaceService);
            furnaceService.getFurnace().stop();
        }
        catch (Throwable t)
        {
            System.err.println("Windup execution failed due to: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static boolean handleAsSystemProperty(String argument)
    {
        if (!argument.startsWith("-D"))
            return false;

        final String name;
        final String value;
        final int index = argument.indexOf("=");
        if (index == -1)
        {
            name = argument.substring(2);
            value = "true";
        }
        else
        {
            name = argument.substring(2, index);
            value = argument.substring(index + 1);
        }
        System.setProperty(name, value);
        return true;
    }

    /**
     * Process some of arguments.
     */
    private void processArguments(List<String> arguments, BootstrapFurnaceService furnaceService)
    {
        final Furnace furnace = furnaceService.getFurnace();
        System.setProperty("forge.standalone", "false");
        BootstrapCommand command = null;

        if (arguments.contains("-help") || arguments.contains("--help") || arguments.contains("-h") ||
                    arguments.contains("/?") || arguments.contains("/help"))
        {
            command = BootstrapCommand.DISPLAY_HELP;
        }
        String addonID = null;
        boolean updateRulesets = false;

        List<String> unknownArgs = new ArrayList<>();
        List<File> mutableRepos = new ArrayList<>();
        List<File> immutableRepos = new ArrayList<>();

        // The rest...
        for (int i = 0; i < arguments.size(); i++)
        {
            final String arg = arguments.get(i);

            // Forge-related args.
            if ("--install".equals(arg) || "-i".equals(arg))
            {
                command = BootstrapCommand.INSTALL_ADDON;
                addonID = arguments.get(++i);
            }
            else if ("--remove".equals(arg) || "-r".equals(arg))
            {
                command = BootstrapCommand.REMOVE_ADDON;
                addonID = arguments.get(++i);
            }
            else if ("--list".equals(arg) || "-l".equals(arg))
            {
                command = BootstrapCommand.LIST_INSTALLED_ADDONS;
            }
            else if ("--addonDir".equals(arg) || "-a".equals(arg))
            {
                mutableRepos.add(new File(arguments.get(++i)));
            }
            else if ("--immutableAddonDir".equals(arg) || "-m".equals(arg))
            {
                immutableRepos.add(new File(arguments.get(++i)));
            }
            else if ("--batchMode".equals(arg) || "-b".equals(arg))
            {
                batchMode = true;
            }
            else if ("--evaluate".equals(arg) || "-e".equals(arg))
            {
                System.out.println("\"" + arg + "\" is no longer required!");
                i++;
            }
            else if ("--debug".equals(arg) || "-d".equals(arg))
            {
                // This is just to avoid the "Unknown option: --debug" message below
            }
            else if ("--version".equals(arg) || "-v".equals(arg))
            {
                System.out.println(getVersionString());
            }
            else if ("--listTags".equals(arg))
            {
                command = BootstrapCommand.LIST_TAGS;
            }
            else if ("--listSourceTechnologies".equals(arg))
            {
                command = BootstrapCommand.LIST_SOURCE_TECHNOLOGIES;
            }
            else if ("--listTargetTechnologies".equals(arg))
            {
                command = BootstrapCommand.LIST_TARGET_TECHNOLOGIES;
            }
            else if ("--generateCompletionData".equals(arg))
            {
                command = BootstrapCommand.GENERATE_COMPLETION_DATA;
            }
            else if (arg.startsWith("--updateRules"))
            {
                updateRulesets = true;
            }
            else
            {
                unknownArgs.add(arg);
            }
        }

        addReposToFurnace(furnace, mutableRepos, immutableRepos);

        setupNonInteractive(furnace);

        for (int i = 0; i < arguments.size(); i++)
        {
            final String arg = arguments.get(i);
            if (unknownArgs.contains(arg))
                arguments.remove(i);
        }

        // Move Windup arguments to --evaluate '...'
        List<String> windupArguments = new ArrayList<>();
        if (!unknownArgs.isEmpty())
        {
            // Pass unknown arguments to Windup (Forge).
            for (String windupArg : unknownArgs)
                windupArguments.add(windupArg);
        }

        // Process Furnace commands.
        if (!containsMutableRepository(furnace.getRepositories()))
        {
            furnaceService.getFurnace().addRepository(AddonRepositoryMode.MUTABLE, new File(getUserWindupDir(), "addons"));
        }

        if (command == null && updateRulesets && onlyRulesetsUpdateRequested(arguments))
            command = BootstrapCommand.UPDATE_RULESETS;

        if (command == null && !windupArguments.isEmpty())
            command = BootstrapCommand.RUN_WINDUP;

        if (command == null)
            command = BootstrapCommand.DISPLAY_HELP;

        switch (command)
        {
            case LIST_INSTALLED_ADDONS:
                furnaceService.list();
                break;
            case INSTALL_ADDON:
                furnaceService.install(addonID, this.batchMode);
                break;
            case REMOVE_ADDON:
                furnaceService.remove(addonID, this.batchMode);
                break;
        }

        // Start Furnace service if we are going to need it.
        try
        {
            if (command == BootstrapCommand.RUN_WINDUP || command == BootstrapCommand.UPDATE_RULESETS)
                furnace.addContainerLifecycleListener(new GreetingListener());
            Future<Furnace> future = furnaceService.start(true);
            // use future.get() to wait until it is started
            future.get();
        }
        catch (Exception e)
        {
            System.out.println("Failed to start Windup!");
            if (e.getMessage() != null)
                System.out.println("Failure reason: " + e.getMessage());
            e.printStackTrace();
        }


        // Update rulesets
        if (updateRulesets)
        {
            // Prevent help from being printed.
            if(command == null)
                command = BootstrapCommand.UPDATE_RULESETS;

            System.out.println("Checking for rulesets updates...");
            try {
                Imported<DependencyResolver> resolver = furnace.getAddonRegistry().getServices(DependencyResolver.class);
                //new RulesetsUpdater(resolver.get(), furnace).replaceRulesetsDirectoryWithLatestReleaseIfAny();
                Imported<RulesetsUpdater> services = furnace.getAddonRegistry().getServices(RulesetsUpdater.class);
                Assert.notNull(services, "Couldn't find the " + RulesetsUpdater.class.getSimpleName() + " service.");
                RulesetsUpdater updater = services.get();
                String newVersion = updater.replaceRulesetsDirectoryWithLatestReleaseIfAny();
                System.out.println("Updated the rulesets to version " + newVersion + ".");
            }
            catch (Throwable ex)
            {
                System.err.println("Could not update the rulesets: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        }


        switch (command)
        {
            case GENERATE_COMPLETION_DATA:
                generateCompletionData(true);
                break;
            case LIST_TAGS:
                listTags();
                break;
            case LIST_SOURCE_TECHNOLOGIES:
                listSourceTechnologies();
                break;
            case LIST_TARGET_TECHNOLOGIES:
                listTargetTechnologies();
                break;
            case RUN_WINDUP:
                runWindup(windupArguments);
                break;
            case UPDATE_RULESETS:
                break;
            case DISPLAY_HELP:
            default:
                displayHelp();
                break;
        }
    }

    private void listTags()
    {
        printValuesSorted("Available tags", getRuleProviderRegistryCache().getAvailableTags());
    }

    private void listSourceTechnologies()
    {
        printValuesSorted("Available source technologies", getRuleProviderRegistryCache().getAvailableSourceTechnologies());
    }

    private void listTargetTechnologies()
    {
        printValuesSorted("Available target technologies", getRuleProviderRegistryCache().getAvailableTargetTechnologies());
    }

    private void printValuesSorted(String message, Set<String> values)
    {
        System.out.println();
        System.out.println(message + ":");
        List<String> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        for (String value : sorted)
        {
            System.out.println("\t" + value);
        }
    }

    private void generateCompletionData(boolean force)
    {
        Path completionPath = PathUtil.getWindupHome().resolve("cache").resolve("bash-completion").resolve("bash-completion.data");
        if (!force && Files.isRegularFile(completionPath))
        {
            try
            {
                FileTime modifiedTime = Files.getLastModifiedTime(completionPath);
                long age = System.currentTimeMillis() - modifiedTime.to(TimeUnit.MILLISECONDS);
                if (age <= MAX_COMPLETION_AGE)
                    return;
            }
            catch (IOException e)
            {
                // ignore it
            }
        }
        try
        {
            if (!Files.isDirectory(completionPath.getParent()))
            {
                Files.createDirectories(completionPath.getParent());
            }
            try (FileWriter writer = new FileWriter(completionPath.toFile()))
            {
                writer.write("listTags:none" + NEW_LINE);
                writer.write("listSourceTechnologies:none" + NEW_LINE);
                writer.write("listTargetTechnologies:none" + NEW_LINE);
                writer.write("install:none" + NEW_LINE);
                writer.write("remote:none" + NEW_LINE);
                writer.write("addonDir:file" + NEW_LINE);
                writer.write("immutableAddonDir:file" + NEW_LINE);
                writer.write("batchMode:none" + NEW_LINE);
                writer.write("debug:none" + NEW_LINE);
                writer.write("help:none" + NEW_LINE);
                writer.write("version:none" + NEW_LINE);

                Iterable<ConfigurationOption> optionIterable = WindupConfiguration.getWindupConfigurationOptions(furnaceService.getFurnace());
                for (ConfigurationOption option : optionIterable)
                {
                    StringBuilder line = new StringBuilder();
                    line.append(option.getName()).append(":");
                    if (File.class.isAssignableFrom(option.getType()))
                        line.append("file");
                    else if (option.getUIType() == InputType.SELECT_MANY || option.getUIType() == InputType.SELECT_ONE)
                    {
                        line.append("list").append(":");
                        for (Object availableValue : option.getAvailableValues())
                            line.append(availableValue).append(" ");
                    }
                    else
                        line.append("none");

                    line.append(NEW_LINE);
                    writer.write(line.toString());
                }
            }
        }
        catch (IOException e)
        {
            System.err.println("WARNING: Unable to create bash completion file in \"" + completionPath + "\" due to: " + e.getMessage());
        }
    }

    private static final String NEW_LINE = OperatingSystemUtils.getLineSeparator();



    @SuppressWarnings("unchecked")
    private void runWindup(List<String> arguments)
    {
        Iterable<ConfigurationOption> optionIterable = WindupConfiguration.getWindupConfigurationOptions(furnaceService.getFurnace());
        Map<String, ConfigurationOption> options = new HashMap<>();
        for (ConfigurationOption option : optionIterable)
            options.put(option.getName().toUpperCase(), option);

        Map<String, Object> optionValues = new HashMap<>();
        for (int i = 0; i < arguments.size(); i++)
        {
            String argument = arguments.get(i);
            String optionName = getOptionName(argument);
            if (optionName == null)
            {
                System.err.println("WARNING: Unrecognized command-line argument: " + argument);
                continue;
            }

            ConfigurationOption option = options.get(optionName.toUpperCase());
            if (option == null)
            {
                System.err.println("WARNING: Unrecognized command-line argument: " + argument);
                continue;
            }

            if (option.getUIType() == InputType.MANY || option.getUIType() == InputType.SELECT_MANY)
            {
                List<Object> values = new ArrayList<>();
                i++;
                while (i < arguments.size())
                {
                    if (getOptionName(arguments.get(i)) != null && options.containsKey(getOptionName(arguments.get(i).toUpperCase())))
                    {
                        // this is the next parameter... back up one and break the loop
                        i--;
                        break;
                    }

                    String valueString = arguments.get(i);
                    // lists are space delimited... split them here
                    for (String value : StringUtils.split(valueString, ' '))
                        values.add(convertType(option.getType(), value));

                    i++;
                }

                /*
                 * This allows us to support specifying a parameter multiple times.
                 *
                 * For example:
                 *
                 * `windup --packages foo --packages bar --packages baz`
                 *
                 * While this is not necessarily the recommended approach, it would be nice for it to work smoothly if
                 * someone does it this way.
                 */
                if (optionValues.containsKey(option.getName()))
                    ((List<Object>) optionValues.get(option.getName())).addAll(values);
                else
                    optionValues.put(option.getName(), values);
            }
            else if (Boolean.class.isAssignableFrom(option.getType()))
            {
                optionValues.put(option.getName(), true);
            }
            else
            {
                String valueString = arguments.get(++i);

                optionValues.put(option.getName(), convertType(option.getType(), valueString));
            }
        }

        setDefaultOutputPath(optionValues);
        boolean validationSuccess = validateOptionValues(options, optionValues);
        if (!validationSuccess)
            return;

        WindupConfiguration windupConfiguration = new WindupConfiguration();
        for (Map.Entry<String, ConfigurationOption> optionEntry : options.entrySet())
        {
            ConfigurationOption option = optionEntry.getValue();
            windupConfiguration.setOptionValue(option.getName(), optionValues.get(option.getName()));
        }

        try
        {
            windupConfiguration.useDefaultDirectories();
        }
        catch (IOException e)
        {
            System.err.println("ERROR: Failed to create default directories due to: " + e.getMessage());
            return;
        }

        Boolean overwrite = (Boolean) windupConfiguration.getOptionMap().get(OverwriteOption.NAME);
        if (overwrite == null)
        {
            overwrite = false;
        }

        if (!overwrite && pathNotEmpty(windupConfiguration.getOutputDirectory().toFile()))
        {
            String promptMsg = "Overwrite all contents of \"" + windupConfiguration.getOutputDirectory().toString()
                        + "\" (anything already in the directory will be deleted)?";
            if (!prompt(promptMsg, false))
            {
                String outputPath = windupConfiguration.getOutputDirectory().toString();
                System.err.println("Files exist in " + outputPath + ", but --overwrite not specified. Aborting!");
                return;
            }
        }

        generateCompletionData(false);

        FileUtils.deleteQuietly(windupConfiguration.getOutputDirectory().toFile());
        Path graphPath = windupConfiguration.getOutputDirectory().resolve("graph");
        try (GraphContext graphContext = getGraphContextFactory().create(graphPath))
        {
            WindupProgressMonitor progressMonitor = new ConsoleProgressMonitor();
            windupConfiguration
                        .setProgressMonitor(progressMonitor)
                        .setGraphContext(graphContext);
            getWindupProcessor().execute(windupConfiguration);

            Path indexHtmlPath = windupConfiguration.getOutputDirectory().resolve("index.html").normalize().toAbsolutePath();
            System.out.println("Windup report created: " + indexHtmlPath + System.getProperty("line.separator")
                        + "              Access it at this URL: " + indexHtmlPath.toUri());
        }
        catch (Exception e)
        {
            System.err.println("Windup Execution failed due to: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateOptionValues(Map<String, ConfigurationOption> options, Map<String, Object> optionValues)
    {
        for (Map.Entry<String, ConfigurationOption> optionEntry : options.entrySet())
        {
            ConfigurationOption option = optionEntry.getValue();
            ValidationResult result = option.validate(optionValues.get(option.getName()));

            switch (result.getLevel())
            {
            case ERROR:
                System.err.println("ERROR: " + result.getMessage());
                return false;
            case PROMPT_TO_CONTINUE:
                if (!prompt(result.getMessage(), result.getPromptDefault()))
                    return false;
                break;
            case WARNING:
                System.err.println("WARNING: " + result.getMessage());
                break;
            case SUCCESS:
                break;
            }
        }
        return true;
    }

    private void setDefaultOutputPath(Map<String, Object> optionValues)
    {
        if (!optionValues.containsKey(OutputPathOption.NAME))
        {
            File inputFile = (File) optionValues.get(InputPathOption.NAME);
            if (inputFile != null)
            {
                File outputFile = new File(inputFile.getAbsoluteFile().getParentFile(), inputFile.getName() + ".report");
                optionValues.put(OutputPathOption.NAME, outputFile);
            }
        }
    }

    private Object convertType(Class<?> type, String input)
    {
        if (File.class.isAssignableFrom(type))
        {
            return new File(input);
        }
        else if (Boolean.class.isAssignableFrom(type))
        {
            return Boolean.valueOf(input);
        }
        else if (String.class.isAssignableFrom(type))
        {
            return input;
        }
        else
        {
            throw new RuntimeException("Internal Error! Unrecognized type " + type.getCanonicalName());
        }
    }

    private boolean prompt(String message, boolean defaultValue)
    {
        if (batchMode)
        {
            return defaultValue;
        }
        else
        {
            String defaultMessage = defaultValue ? " [Y,n] " : " [y,N] ";
            String line = System.console().readLine(message + defaultMessage).trim();
            if ("y".equalsIgnoreCase(line))
                return true;
            if ("n".equalsIgnoreCase(line))
                return false;
            return defaultValue;
        }
    }

    private boolean pathNotEmpty(File f)
    {
        if (f.exists() && !f.isDirectory())
        {
            return true;
        }
        if (f.isDirectory() && f.listFiles() != null && f.listFiles().length > 0)
        {
            return true;
        }
        return false;
    }

    private RuleProviderRegistryCache getRuleProviderRegistryCache()
    {
        return this.furnaceService.getFurnace().getAddonRegistry().getServices(RuleProviderRegistryCache.class).get();
    }

    private WindupProcessor getWindupProcessor()
    {
        return this.furnaceService.getFurnace().getAddonRegistry().getServices(WindupProcessor.class).get();
    }

    private GraphContextFactory getGraphContextFactory()
    {
        return this.furnaceService.getFurnace().getAddonRegistry().getServices(GraphContextFactory.class).get();
    }

    private String getOptionName(String argument)
    {
        if (argument.startsWith("--"))
            return argument.substring(2);
        else if (argument.startsWith("-"))
            return argument.substring(1);
        else
            return null;
    }

    private void displayHelp()
    {
        Iterable<ConfigurationOption> windupOptions = WindupConfiguration.getWindupConfigurationOptions(furnaceService.getFurnace());

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
        furnaceService.getFurnace().stop();
    }

    private static void addReposToFurnace(final Furnace furnace, List<File> mutableRepos, List<File> immutableRepos)
    {
        for (File repo : mutableRepos)
        {
            furnace.addRepository(AddonRepositoryMode.MUTABLE, repo);
        }
        for (File repo : immutableRepos)
        {
            furnace.addRepository(AddonRepositoryMode.IMMUTABLE, repo);
        }
    }

    private static void setupNonInteractive(final Furnace furnace)
    {
        furnace.setServerMode(true);
        System.setProperty("INTERACTIVE", "false");
        System.setProperty("forge.shell.evaluate", "true");
    }

    private static boolean containsMutableRepository(List<AddonRepository> repositories)
    {
        boolean result = false;
        for (AddonRepository repository : repositories)
        {
            if (repository instanceof MutableAddonRepository)
            {
                result = true;
                break;
            }
        }
        return result;
    }

    private static String getServiceName(final ClassLoader classLoader, final String className)
    {
        try (final InputStream stream = classLoader.getResourceAsStream("META-INF/services/" + className))
        {
            if (stream == null)
                return null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    // Cut off commennt.
                    final int i = line.indexOf('#');
                    if (i != -1)
                        line = line.substring(0, i);

                    line = line.trim();
                    if (line.length() == 0)
                        continue;

                    return line;
                }
            }
        }
        catch (IOException e)
        {
            // ignore
        }
        return null;
    }

    public static File getUserWindupDir()
    {
        return new File(OperatingSystemUtils.getUserHomeDir(), ".windup").getAbsoluteFile();
    }

    public static String getVersion()
    {
        return getRuntimeAPIVersion().toString();
    }

    public static String getVersionString()
    {
        return "> JBoss Windup, version " + getRuntimeAPIVersion() + ". JBoss Forge, version "
                    + AddonRepositoryImpl.getRuntimeAPIVersion();
    }

    public static Version getRuntimeAPIVersion()
    {
        String version = Bootstrap.class.getPackage().getImplementationVersion();
        if (version != null)
        {
            return new SingleVersion(version);
        }
        return EmptyVersion.getInstance();
    }


    private boolean onlyRulesetsUpdateRequested(List<String> arguments)
    {
        List<String> nonActionableArguments = Arrays.asList(new String[]{
            "--debug", "--addonDir", "--immutableAddonDir", "--batchMode"
        });

        for (String arg : arguments)
        {
            if (!arg.startsWith("-"))
                continue;
            if (arg.startsWith("--updateRules"))
                continue;
            if (nonActionableArguments.contains(arg))
                continue;
            return false;
        }

        return true;
    }
}
