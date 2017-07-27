/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.windup.bootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.impl.addons.AddonRepositoryImpl;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.repositories.MutableAddonRepository;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.forge.furnace.versions.EmptyVersion;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.bootstrap.commands.addons.AddAddonDirectoryCommand;
import org.jboss.windup.bootstrap.commands.addons.AddImmutableAddonDirectoryCommand;
import org.jboss.windup.bootstrap.commands.addons.InstallAddonCommand;
import org.jboss.windup.bootstrap.commands.addons.ListAddonsCommand;
import org.jboss.windup.bootstrap.commands.addons.RemoveAddonCommand;
import org.jboss.windup.bootstrap.commands.windup.DiscoverPackagesCommand;
import org.jboss.windup.bootstrap.commands.windup.DisplayHelpCommand;
import org.jboss.windup.bootstrap.commands.windup.DisplayVersionCommand;
import org.jboss.windup.bootstrap.commands.windup.GenerateCompletionDataCommand;
import org.jboss.windup.bootstrap.commands.windup.GenerateHelpCacheCommand;
import org.jboss.windup.bootstrap.commands.windup.ListSourceTechnologiesCommand;
import org.jboss.windup.bootstrap.commands.windup.ListTagsCommand;
import org.jboss.windup.bootstrap.commands.windup.ListTargetTechnologiesCommand;
import org.jboss.windup.bootstrap.commands.windup.RunWindupCommand;
import org.jboss.windup.bootstrap.commands.windup.ServerModeCommand;
import org.jboss.windup.bootstrap.commands.windup.UpdateRulesetsCommand;
import org.jboss.windup.bootstrap.listener.GreetingListener;
import org.jboss.windup.util.Util;

/**
 * A class with a main method to bootstrap Windup.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Bootstrap
{
    public static final String WINDUP_HOME = "windup.home";
    private final AtomicBoolean batchMode = new AtomicBoolean(false);
    private Furnace furnace;

    public static void main(final String[] args)
    {
        final List<String> bootstrapArgs = new ArrayList<>();

        for (String arg : args)
        {
            if (!handleAsSystemProperty(arg))
                bootstrapArgs.add(arg);
        }

        File rulesDir = getUserRulesDir();
        if (!rulesDir.exists())
        {
            rulesDir.mkdirs();
        }

        final String defaultLog = new File(getUserWindupDir(), "log/rhamt.log").getAbsolutePath();
        final String logDir = System.getProperty("org.jboss.forge.log.file", defaultLog);

        System.setProperty("org.jboss.forge.log.file", logDir);

        final String logManagerName = getServiceName(Bootstrap.class.getClassLoader(), "java.util.logging.LogManager");
        if (logManagerName != null)
        {
            System.setProperty("java.util.logging.manager", logManagerName);
        }

        Bootstrap bootstrap = new Bootstrap();
        if (!bootstrap.serverMode(bootstrapArgs))
        {
            bootstrap.run(bootstrapArgs);
            bootstrap.stop();
        }
    }

    private static boolean handleAsSystemProperty(String argument)
    {
        if (!argument.startsWith("-D"))
            return false;

        final String name;
        final String value;
        final int index = argument.indexOf('=');
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

    public static String promptForListItem(String message, Collection<String> items, String defaultValue)
    {
        while (true)
        {
            List<String> sorted = new ArrayList<>(items);
            Collections.sort(sorted);

            System.out.println();
            System.out.println(message);
            for (String item : sorted)
            {
                System.out.println("\t" + item);
            }

            String promptMessage = "Please enter the item you would like to choose[" + defaultValue + "]: ";
            String item = System.console().readLine(promptMessage).trim();
            if (StringUtils.isNotBlank(item))
                return item;
            else if (StringUtils.isNotBlank(defaultValue))
                return defaultValue;
            else
                System.out.println("A selection is required. Please select one of the available items.");
        }
    }

    public static boolean prompt(String message, boolean defaultValue, boolean batchMode)
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
                    /*
                     * Ignore comments in the file.
                     */
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

    public static String getVersion()
    {
        return getRuntimeAPIVersion().toString();
    }

    public static String getVersionString()
    {
        return "> Red Hat Application Migration Toolkit (RHAMT) CLI, version " + getRuntimeAPIVersion() + ".";
    }

    public static Version getRuntimeAPIVersion()
    {
        String version = Bootstrap.class.getPackage().getImplementationVersion();
        if (version != null)
        {
            return SingleVersion.valueOf(version);
        }
        return EmptyVersion.getInstance();
    }

    private static File getUserRulesDir()
    {
        return new File(getUserWindupDir(), "rules");
    }

    public static File getUserWindupDir()
    {
        String userHome = System.getProperty("user.home");
        if (userHome == null)
        {
            Path path = new File("").toPath();
            return path.toFile();
        }
        return Paths.get(userHome).resolve(".rhamt").toFile();
    }

    private static File getUserAddonsDir()
    {
        return getUserWindupDir().toPath().resolve(".addons").toFile();
    }

    private boolean serverMode(List<String> arguments)
    {
        if (ServerModeCommand.isServerMode(arguments))
        {
            ServerModeCommand serverCommand = new ServerModeCommand(arguments);
            serverCommand.execute();
            return true;
        }
        return false;
    }

    private void run(List<String> args)
    {
        try
        {
            furnace = FurnaceFactory.getInstance();
            furnace.setServerMode(true);

            CopyOnWriteArrayList<Command> commands = new CopyOnWriteArrayList<>(processArguments(args));

            if (!executePhase(CommandPhase.PRE_CONFIGURATION, commands))
                return;

            if (!executePhase(CommandPhase.CONFIGURATION, commands))
                return;

            if (commands.isEmpty())
            {
                // no commands are available, just print the help and exit
                new DisplayHelpCommand().execute();
                return;
            }

            if (!containsMutableRepository(furnace.getRepositories()))
            {
                furnace.addRepository(AddonRepositoryMode.MUTABLE, getUserAddonsDir());
            }

            if (!executePhase(CommandPhase.POST_CONFIGURATION, commands) || commands.isEmpty())
                return;

            try
            {
                Future<Furnace> future = furnace.startAsync();
                future.get(); // use future.get() to wait until it is started
            }
            catch (Exception e)
            {
                System.out.println("Failed to start "+ Util.WINDUP_BRAND_NAME_ACRONYM+"!");
                if (e.getMessage() != null)
                    System.out.println("Failure reason: " + e.getMessage());
                e.printStackTrace();
            }

            if (!executePhase(CommandPhase.PRE_EXECUTION, commands) || commands.isEmpty())
                return;

            furnace.addContainerLifecycleListener(new GreetingListener());

            if (!executePhase(CommandPhase.EXECUTION, commands) || commands.isEmpty())
                return;

            if (!executePhase(CommandPhase.POST_EXECUTION, commands) || commands.isEmpty())
                return;
        }
        catch (Throwable t)
        {
            System.err.println(Util.WINDUP_BRAND_NAME_ACRONYM +" execution failed due to: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void stop()
    {
        if (furnace != null && !furnace.getStatus().isStopped())
            furnace.stop();
    }

    private List<Command> processArguments(List<String> arguments)
    {
        List<String> unknownArgs = new ArrayList<>();

        List<Command> commands = new ArrayList<>();

        boolean versionCommandAdded = false;
        for (int i = 0; i < arguments.size(); i++)
        {
            final String arg = arguments.get(i);

            if ("--batchMode".equals(arg) || "-b".equals(arg))
            {
                batchMode.set(true);
            }
            else if ("--debug".equals(arg) || "-d".equals(arg))
            {
                /*
                 * This is to avoid the "Unknown option: --debug" message generated by Windup when it receives an option it doesn't understand.
                 */
            }
            else if (arg.equals("-help") || arg.equals("--help") || arg.equals("-h") ||
                        arg.equals("/?") || arg.equals("/help"))
            {
                commands.add(new DisplayHelpCommand());
            }
            else if ("--install".equals(arg) || "-i".equals(arg))
            {
                commands.add(new InstallAddonCommand(arguments.get(++i), batchMode));
            }
            else if ("--remove".equals(arg) || "-r".equals(arg))
            {
                commands.add(new RemoveAddonCommand(arguments.get(++i), batchMode));
            }
            else if ("--list".equals(arg) || "-l".equals(arg))
            {
                commands.add(new ListAddonsCommand());
            }
            else if ("--addonDir".equals(arg) || "-a".equals(arg))
            {
                commands.add(new AddAddonDirectoryCommand(arguments.get(++i)));
            }
            else if ("--immutableAddonDir".equals(arg) || "-m".equals(arg))
            {
                commands.add(new AddImmutableAddonDirectoryCommand(arguments.get(++i)));
            }
            else if ("--version".equals(arg) || "-v".equals(arg))
            {
                versionCommandAdded = true;
                commands.add(new DisplayVersionCommand());
            }
            else if ("--listTags".equals(arg))
            {
                commands.add(new ListTagsCommand());
            }
            else if ("--listSourceTechnologies".equals(arg))
            {
                commands.add(new ListSourceTechnologiesCommand());
            }
            else if ("--listTargetTechnologies".equals(arg))
            {
                commands.add(new ListTargetTechnologiesCommand());
            }
            else if ("--generateCompletionData".equals(arg))
            {
                commands.add(new GenerateCompletionDataCommand(true));
            }
            else if (arg.equals("--generateHelp"))
            {
                commands.add(new GenerateHelpCacheCommand());
            }
            else if ("--generateCaches".equals(arg))
            {
                commands.add(new GenerateCompletionDataCommand(true));
                commands.add(new GenerateHelpCacheCommand());
            }
            else if ("--discoverPackages".equals(arg))
            {
                unknownArgs.add(arg);
                commands.add(new DiscoverPackagesCommand(unknownArgs));
            }
            else if (arg.startsWith("--updateRules"))
            {
                commands.add(new UpdateRulesetsCommand());
            }
            else
            {
                unknownArgs.add(arg);
            }
        }
        if (!versionCommandAdded)
            commands.add(0, new DisplayVersionCommand(CommandResult.CONTINUE));

        List<String> windupArguments = new ArrayList<>(unknownArgs);
        if (!windupArguments.isEmpty())
        {
            // go ahead and regenerate this every time just in case there are user-added addons that affect the result
            commands.add(new GenerateHelpCacheCommand());
            commands.add(new GenerateCompletionDataCommand(true));

            commands.add(new RunWindupCommand(windupArguments, batchMode));
        }

        return commands;
    }

    private boolean executePhase(CommandPhase phase, CopyOnWriteArrayList<Command> commands)
    {
        for (Command command : commands)
        {
            if (phase.equals(command.getPhase()))
            {
                commands.remove(command);
                if (command instanceof FurnaceDependent)
                    ((FurnaceDependent) command).setFurnace(furnace);

                CommandResult result = command.execute();
                if (CommandResult.EXIT.equals(result))
                    return false;
            }
        }
        return true;
    }

    private File getWindupAddonsDir()
    {
        return new File(getWindupHome(), "addons");
    }

    private File getWindupHome()
    {
        String windupHome = System.getProperty(WINDUP_HOME);
        if (windupHome == null)
        {
            Path path = new File("").toPath();
            return path.toFile();
        }
        return Paths.get(windupHome).toFile();
    }
}
