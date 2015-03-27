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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.impl.addons.AddonRepositoryImpl;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.repositories.MutableAddonRepository;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.furnace.versions.EmptyVersion;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.windup.bootstrap.listener.GreetingListener;

/**
 * A class with a main method to bootstrap Windup.
 *
 * You can deploy addons by calling {@link Bootstrap#install(String)}
 *
 * @author mbriskar
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Bootstrap
{
    private BootstrapFurnaceService furnaceService = null;

    private boolean exitAfter = false;
    private boolean batchMode = false;

    public static void main(final String[] args) throws InterruptedException, ExecutionException
    {
        final List<String> bootstrapArgs = new ArrayList<>();
        final Properties systemProperties = System.getProperties();

        // For all arguments...
        for (String arg : args)
        {
            // Turn -D...[=...] into system properties
            if (arg.startsWith("-D"))
            {
                final String name;
                final String value;
                final int index = arg.indexOf("=");
                if (index == -1)
                {
                    name = arg.substring(2);
                    value = "true";
                }
                else
                {
                    name = arg.substring(2, index);
                    value = arg.substring(index + 1);
                }
                systemProperties.setProperty(name, value);
            }
            else
            {
                bootstrapArgs.add(arg);
            }
        }

        // Ensure user rules directory is created
        File rulesDir = new File(getUserWindupDir(), "rules");
        if (!rulesDir.exists())
        {
            rulesDir.mkdirs();
        }

        // Check for the forge log directory
        final String defaultLog = new File(getUserWindupDir(), "log/windup.log").getAbsolutePath();
        final String logDir = systemProperties.getProperty("org.jboss.forge.log.file", defaultLog);

        // Ensure this value is always set
        systemProperties.setProperty("org.jboss.forge.log.file", logDir);

        // Look for a logmanager before any logging takes place
        final String logManagerName = getServiceName(Bootstrap.class.getClassLoader(), "java.util.logging.LogManager");
        if (logManagerName != null)
        {
            systemProperties.setProperty("java.util.logging.manager", logManagerName);
        }
        Bootstrap bootstrap = new Bootstrap(bootstrapArgs.toArray(new String[bootstrapArgs.size()]));
        
        bootstrap.start();
    }

    /**
     * Process some of arguments.
     */
    private String[] processArguments(String[] args, BootstrapFurnaceService furnaceService)
    {
        final Furnace furnace = furnaceService.getFurnace();
        System.setProperty("forge.standalone", "false");
        boolean displayHelp = false;

        // --help
        List<String> listArgs = Arrays.asList(args);
        if (listArgs.contains("-help") || listArgs.contains("--help") || listArgs.contains("-h") || listArgs.contains("/?")
                    || listArgs.contains("/help"))
        {
            displayHelp = true;
            exitAfter = true;
        }

        boolean listInstalled = false;
        String installAddon = null;
        String removeAddon = null;

        boolean isEvaluate = false;

        List<String> unknownArgs = new ArrayList<>();
        List<File> mutableRepos = new ArrayList<>();
        List<File> immutableRepos = new ArrayList<>();

        // The rest...
        for (int i = 0; i < args.length; i++)
        {
            final String arg = args[i];

            // Forge-related args.
            if ("--install".equals(arg) || "-i".equals(arg))
            {
                installAddon = args[++i];
            }
            else if ("--remove".equals(arg) || "-r".equals(arg))
            {
                removeAddon = args[++i];
            }
            else if ("--list".equals(arg) || "-l".equals(arg))
            {
                listInstalled = true;
            }
            else if ("--addonDir".equals(arg) || "-a".equals(arg))
            {
                mutableRepos.add(new File(args[++i]));
            }
            else if ("--immutableAddonDir".equals(arg) || "-m".equals(arg))
            {
                immutableRepos.add(new File(args[++i]));
            }
            else if ("--batchMode".equals(arg) || "-b".equals(arg))
            {
                batchMode = true;
            }
            else if ("--evaluate".equals(arg) || "-e".equals(arg))
            {
                isEvaluate = true;
                i++;
            }
            else if ("--debug".equals(arg) || "-d".equals(arg))
            {
                // This is just to avoid the "Unknown option: --debug" message below
            }
            else if ("--version".equals(arg) || "-v".equals(arg))
            {
                System.out.println(getVersionString());
                this.exitAfter = true;
            }
            else
            {
                unknownArgs.add(arg);
            }
        }

        addReposToFurnace(furnace, mutableRepos, immutableRepos);

        furnace.addContainerLifecycleListener(new GreetingListener(displayHelp));

        if (isEvaluate)
            setupNonInteractive(furnace);

        if (batchMode)
        {
            furnace.setServerMode(false);
        }

        if (displayHelp)
        {
            // The main purpose of this run is only to show the options. No need to provide all the arguments.
            return args;
        }

        for (int i = 0; i < args.length; i++)
        {
            final String arg = args[i];
            if (unknownArgs.contains(arg))
            {
                args[i] = null;
            }
        }

        // Make it a List, Get rid of nulls.
        List<String> argsList = new ArrayList<>(args.length + 2);
        for (String arg : args)
        {
            if (arg != null)
                argsList.add(arg);
        }

        // Move Windup arguments to --evaluate '...'
        if (!unknownArgs.isEmpty())
        {
            setupNonInteractive(furnace);

            // Pass unknown arguments to Windup (Forge).

            if (isEvaluate)
                System.out.println("Both --evaluate (-e) and Windup options were found, may lead to unexpected behavior.");

            StringBuilder sb = new StringBuilder("windup-migrate-app");
            for (String windupArg : unknownArgs)
                sb.append(" ").append(windupArg);
            argsList.add("-e");
            argsList.add(sb.toString());
        }
        else
        {
            if (!isEvaluate)
            {
                System.setProperty("forge.standalone", "true");
            }
        }

        args = (String[]) argsList.toArray(new String[argsList.size()]);

        // Process Furnace commands.
        if (!containsMutableRepository(furnace.getRepositories()))
        {
            furnaceService.getFurnace().addRepository(AddonRepositoryMode.MUTABLE, new File(getUserWindupDir(), "addons"));
        }
        if (listInstalled)
        {
            this.exitAfter = furnaceService.list();
        }
        if (removeAddon != null)
        {
            this.exitAfter = furnaceService.remove(removeAddon, this.exitAfter);
        }
        if (installAddon != null)
        {
            this.exitAfter = furnaceService.install(installAddon, this.exitAfter);
        }

        return args;
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

    /**
     * Initialize Furnace and process some of arguments.
     */
    private Bootstrap(String[] args)
    {
        Furnace furnace = FurnaceFactory.getInstance();
        furnaceService = new BootstrapFurnaceService(furnace);

        args = processArguments(args, furnaceService);
        furnace.setArgs(args);
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

    private void start() throws InterruptedException, ExecutionException
    {
        furnaceService.start(exitAfter, batchMode);
    }

}
