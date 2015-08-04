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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.jboss.windup.bootstrap.commands.windup.DisplayHelpCommand;
import org.jboss.windup.bootstrap.commands.windup.DisplayVersionCommand;
import org.jboss.windup.bootstrap.commands.windup.GenerateCompletionDataCommand;
import org.jboss.windup.bootstrap.commands.windup.ListSourceTechnologiesCommand;
import org.jboss.windup.bootstrap.commands.windup.ListTagsCommand;
import org.jboss.windup.bootstrap.commands.windup.ListTargetTechnologiesCommand;
import org.jboss.windup.bootstrap.commands.windup.RunWindupCommand;
import org.jboss.windup.bootstrap.commands.windup.UpdateRulesetsCommand;
import org.jboss.windup.bootstrap.listener.GreetingListener;

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
    private AtomicBoolean batchMode = new AtomicBoolean(false);
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

        final String defaultLog = new File(getUserWindupDir(), "log/windup.log").getAbsolutePath();
        final String logDir = System.getProperty("org.jboss.forge.log.file", defaultLog);

        System.setProperty("org.jboss.forge.log.file", logDir);

        final String logManagerName = getServiceName(Bootstrap.class.getClassLoader(), "java.util.logging.LogManager");
        if (logManagerName != null)
        {
            System.setProperty("java.util.logging.manager", logManagerName);
        }

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.run(bootstrapArgs);
        bootstrap.stop();
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

            if (!containsMutableRepository(furnace.getRepositories()))
            {
                furnace.addRepository(AddonRepositoryMode.MUTABLE, getWindupAddonsDir());
            }

            if (commands.isEmpty())
                commands.add(new DisplayHelpCommand());

            if (!executePhase(CommandPhase.POST_CONFIGURATION, commands) || commands.isEmpty())
                return;

            try
            {
                Future<Furnace> future = furnace.startAsync();
                future.get(); // use future.get() to wait until it is started
            }
            catch (Exception e)
            {
                System.out.println("Failed to start Windup!");
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
            System.err.println("Windup execution failed due to: " + t.getMessage());
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
                 * This is to avoid the "Unknown option: --debug" message generated by Windup when it receives an option
                 * it doesn't understand.
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
            else if (arg.startsWith("--updateRules"))
            {
                commands.add(new UpdateRulesetsCommand());
            }
            else
            {
                unknownArgs.add(arg);
            }
        }

        for (int i = 0; i < arguments.size(); i++)
        {
            final String arg = arguments.get(i);
            if (unknownArgs.contains(arg))
                arguments.remove(i);
        }

        List<String> windupArguments = new ArrayList<>(unknownArgs);
        if (!windupArguments.isEmpty())
        {
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
        return Paths.get(userHome).resolve(".windup").toFile();
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
