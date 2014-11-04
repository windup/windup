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
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.impl.addons.AddonRepositoryImpl;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.repositories.MutableAddonRepository;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.furnace.versions.EmptyVersion;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.Version;

/**
 * A class with a main method to bootstrap Windup.
 *
 * You can deploy addons by calling {@link Bootstrap#install(String)}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Bootstrap
{
    private final BootstrapFurnaceService furnaceService;

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
        if(!rulesDir.exists())
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
     *
     */
    private Bootstrap(String[] args)
    {
        boolean listInstalled = false;
        String installAddon = null;
        String removeAddon = null;
        Furnace furnace = ServiceLoader.load(Furnace.class).iterator().next();
        furnaceService = new BootstrapFurnaceService(furnace);

        furnace.setArgs(args);

        // --help
        List<String> listArgs = Arrays.asList(args);
        if (listArgs.contains("--help") || listArgs.contains("-h"))
        {
            System.out.println(help());
            exitAfter = true;
            return;
        }

        // The rest...
        for (int i = 0; i < args.length; i++)
        {
            if ("--install".equals(args[i]) || "-i".equals(args[i]))
            {
                installAddon = args[++i];
            }
            else if ("--remove".equals(args[i]) || "-r".equals(args[i]))
            {
                removeAddon = args[++i];
            }
            else if ("--list".equals(args[i]) || "-l".equals(args[i]))
            {
                listInstalled = true;
            }
            else if ("--addonDir".equals(args[i]) || "-a".equals(args[i]))
            {
                furnace.addRepository(AddonRepositoryMode.MUTABLE, new File(args[++i]));
            }
            else if ("--immutableAddonDir".equals(args[i]) || "-m".equals(args[i]))
            {
                furnace.addRepository(AddonRepositoryMode.IMMUTABLE, new File(args[++i]));
            }
            else if ("--batchMode".equals(args[i]) || "-b".equals(args[i]))
            {
                batchMode = true;
                furnace.setServerMode(false);
            }
            else if ("--evaluate".equals(args[i]) || "-e".equals(args[i]))
            {
                furnace.setServerMode(true);
                System.setProperty("INTERACTIVE", "false");
                System.setProperty("forge.shell.evaluate", "true");
                i++;
            }
            else if ("--debug".equals(args[i]) || "-d".equals(args[i]))
            {
                // This is just to avoid the Unknown option: --debug message below
            }
            else if ("--version".equals(args[i]) || "-v".equals(args[i]))
            {
                System.out.println(getVersionString());
                this.exitAfter = true;
            }
            else
            {
                System.out.println("Windup: unrecognized option: '" + args[i] + "'");
                System.out.println("Try 'windup --help' for more information.");
                this.exitAfter = true;
            }
        }

        if (!containsMutableRepository(furnace.getRepositories()))
        {
            furnace.addRepository(AddonRepositoryMode.MUTABLE, new File(getUserWindupDir(), "addons"));
        }
        if (listInstalled)
        {
            this.exitAfter = furnaceService.list();
        }
        if (installAddon != null)
        {
            furnaceService.install(installAddon, this.exitAfter);
        }
        if (removeAddon != null)
        {
            furnaceService.remove(removeAddon, this.exitAfter);
        }
    }

    private String help()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: windup [OPTION]... PARAMETER ... \n");
        sb.append("Extendable migration analysis, at your fingertips. \n");
        sb.append("\n");

        sb.append("-i, --install [[groupId:]addon[,version]]\n");
        sb.append("\t install the required addons and exit. ex: `windup -i core-addon-x` or `windup -i org.example.addon:example,1.0.0` \n");

        sb.append("-r, --remove [[groupId:]addon[,version]]\n");
        sb.append("\t remove the required addons and exit. ex: `windup -r core-addon-x` or `windup -r org.example.addon:example,1.0.0` \n");

        sb.append("-l, --list\n");
        sb.append("\t list installed addons and exit \n");

        sb.append("-a, --addonDir [dir]\n");
        sb.append("\t add the given directory for use as a custom addon repository \n");

        sb.append("-e, --evaluate [cmd]\n");
        sb.append("\t evaluate the given string as commands (requires shell addon. Install via: `windup -i shell`) \n");

        sb.append("-m, --immutableAddonDir [dir]\n");
        sb.append("\t add the given directory for use as a custom immutable addon repository (read only) \n");

        sb.append("-b, --batchMode\n");
        sb.append("\t run Forge in batch mode and does not prompt for confirmation (exits immediately after running) \n");

        sb.append("-d, --debug\n");
        sb.append("\t run Forge in debug mode (wait on port 8000 for a debugger to attach) \n");

        sb.append("-h, --help\n");
        sb.append("\t display this help and exit \n");

        sb.append("-v, --version\n");
        sb.append("\t output version information and exit \n");
        return sb.toString();
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
            catch (IOException e)
            {
                // ignore
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
