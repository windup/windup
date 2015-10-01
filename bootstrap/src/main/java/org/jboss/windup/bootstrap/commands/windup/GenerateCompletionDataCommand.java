package org.jboss.windup.bootstrap.commands.windup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.config.ConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.util.PathUtil;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class GenerateCompletionDataCommand implements Command, FurnaceDependent
{
    private static final String NEW_LINE = OperatingSystemUtils.getLineSeparator();
    private static final long MAX_COMPLETION_AGE = 10000L * 60L * 60L * 24L;

    private Furnace furnace;
    private boolean overwrite;

    public GenerateCompletionDataCommand(boolean overwrite)
    {
        this.overwrite = overwrite;
    }

    @Override
    public void setFurnace(Furnace furnace)
    {
        this.furnace = furnace;
    }

    @Override
    public CommandResult execute()
    {
        generateCompletionData(overwrite);
        return CommandResult.CONTINUE;
    }

    private void generateCompletionData(boolean overwrite)
    {
        Path completionPath = PathUtil.getWindupHome().resolve("cache").resolve("bash-completion").resolve("bash-completion.data");
        if (!overwrite && Files.isRegularFile(completionPath))
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

                Iterable<ConfigurationOption> optionIterable = WindupConfiguration.getWindupConfigurationOptions(furnace);
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

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.PRE_EXECUTION;
    }
}
