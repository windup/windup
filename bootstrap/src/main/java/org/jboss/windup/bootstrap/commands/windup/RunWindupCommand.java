package org.jboss.windup.bootstrap.commands.windup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.bootstrap.Bootstrap;
import org.jboss.windup.bootstrap.ConsoleProgressMonitor;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.KeepWorkDirsOption;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.exec.configuration.options.OutputPathOption;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;


public class RunWindupCommand implements Command, FurnaceDependent
{
    private static final Logger log = Logging.get(RunWindupCommand.class);

    private static final String GRAPH_DATA_SUBDIR = "graph";

    private Furnace furnace;
    private final List<String> arguments;
    private final AtomicBoolean batchMode;

    public RunWindupCommand(List<String> arguments, AtomicBoolean batchMode)
    {
        this.arguments = arguments;
        this.batchMode = batchMode;
    }

    @Override
    public CommandResult execute()
    {
        runWindup(arguments);
        return CommandResult.EXIT;
    }

    @Override
    public void setFurnace(Furnace furnace)
    {
        this.furnace = furnace;
    }

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.EXECUTION;
    }

    @SuppressWarnings("unchecked")
    private void runWindup(List<String> arguments)
    {
        Iterable<ConfigurationOption> optionIterable = WindupConfiguration.getWindupConfigurationOptions(furnace);
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

        RuleProviderRegistryCache ruleProviderRegistryCache = furnace.getAddonRegistry().getServices(RuleProviderRegistryCache.class).get();

        Collection<String> targets = (Collection<String>) optionValues.get(TargetOption.NAME);
        if ((targets == null || targets.isEmpty()) && !batchMode.get())
        {
            String target = Bootstrap.promptForListItem("Please select a target:", ruleProviderRegistryCache.getAvailableTargetTechnologies(), "eap");
            targets = Collections.singleton(target);
            optionValues.put(TargetOption.NAME, targets);
        }

        boolean validationSuccess = validateOptionValues(options, optionValues);
        if (!validationSuccess)
            return;

        WindupConfiguration windupConfiguration = new WindupConfiguration();
        for (Map.Entry<String, ConfigurationOption> optionEntry : options.entrySet())
        {
            ConfigurationOption option = optionEntry.getValue();
            windupConfiguration.setOptionValue(option.getName(), optionValues.get(option.getName()));
        }

        if (!validateInputAndOutputPath(windupConfiguration.getInputPaths().iterator().next(), windupConfiguration.getOutputDirectory()))
            return;

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
            if (!Bootstrap.prompt(promptMsg, false, batchMode.get()))
            {
                String outputPath = windupConfiguration.getOutputDirectory().toString();
                System.err.println("Files exist in " + outputPath + ", but --overwrite not specified. Aborting!");
                return;
            }
        }

        GenerateCompletionDataCommand generateCompletionDataCommand = new GenerateCompletionDataCommand(false);
        generateCompletionDataCommand.setFurnace(furnace);
        generateCompletionDataCommand.execute();

        FileUtils.deleteQuietly(windupConfiguration.getOutputDirectory().toFile());
        Path graphPath = windupConfiguration.getOutputDirectory().resolve(GRAPH_DATA_SUBDIR);
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

            deleteGraphDataUnlessInhibited(windupConfiguration, graphPath);
        }
        catch (Exception e)
        {
            System.err.println("Windup Execution failed due to: " + e.getMessage());
            e.printStackTrace();
        }

    }


    private boolean validateInputAndOutputPath(Path inputPath, Path outputPath)
    {
        ValidationResult validationResult = OutputPathOption.validateInputAndOutputPath(inputPath, outputPath);
        switch (validationResult.getLevel())
        {
        case ERROR:
            System.err.println("ERROR: " + validationResult.getMessage());
            return false;
        case WARNING:
            System.err.println("WARNING: " + validationResult.getMessage());
        default:
            return true;
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
                if (!Bootstrap.prompt(result.getMessage(), result.getPromptDefault(), batchMode.get()))
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
                try
                {
                    File canonicalInputFile = inputFile.getCanonicalFile();
                    File outputFile = new File(canonicalInputFile.getParentFile(), canonicalInputFile.getName() + ".report");
                    optionValues.put(OutputPathOption.NAME, outputFile);
                }
                catch (IOException e)
                {
                    throw new WindupException("Failed to get canonical path for input file: " + inputFile);
                }
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

    private WindupProcessor getWindupProcessor()
    {
        return furnace.getAddonRegistry().getServices(WindupProcessor.class).get();
    }

    private GraphContextFactory getGraphContextFactory()
    {
        return furnace.getAddonRegistry().getServices(GraphContextFactory.class).get();
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


    private void deleteGraphDataUnlessInhibited(WindupConfiguration windupConfiguration, Path graphPath)
    {
        Boolean keep = (Boolean) windupConfiguration.getOptionMap().get(KeepWorkDirsOption.NAME);
        if (keep == null || !keep)
        {
            log.info("Deleting graph directory (see --" + KeepWorkDirsOption.NAME + "): " + graphPath.toFile().getPath());
            try
            {
                FileUtils.deleteDirectory(graphPath.toFile());
            }
            catch (IOException ex)
            {
                log.log(Level.WARNING, "Failed deleting graph directory: " + graphPath.toFile().getPath()
                    + "\n\tDue to: " + ex.getMessage(), ex);
            }
        }
    }

}
