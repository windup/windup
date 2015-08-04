package org.jboss.windup.bootstrap.commands.windup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.bootstrap.ConsoleProgressMonitor;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.exec.configuration.options.OutputPathOption;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;

public class RunWindupCommand implements Command, FurnaceDependent
{
    private Furnace furnace;
    private List<String> arguments;
    private AtomicBoolean batchMode;

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

        GenerateCompletionDataCommand generateCompletionDataCommand = new GenerateCompletionDataCommand(false);
        generateCompletionDataCommand.setFurnace(furnace);
        generateCompletionDataCommand.execute();

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
        if (batchMode.get())
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
}
