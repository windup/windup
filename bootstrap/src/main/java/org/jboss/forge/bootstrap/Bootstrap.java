/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.bootstrap;

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
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.impl.addons.AddonRepositoryImpl;
import org.jboss.forge.furnace.manager.impl.AddonManagerImpl;
import org.jboss.forge.furnace.manager.maven.addon.MavenAddonDependencyResolver;
import org.jboss.forge.furnace.manager.request.AddonActionRequest;
import org.jboss.forge.furnace.manager.request.RemoveRequest;
import org.jboss.forge.furnace.manager.spi.AddonDependencyResolver;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.repositories.MutableAddonRepository;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.forge.furnace.versions.Versions;

/**
 * A class with a main method to bootstrap Furnace.
 *
 * You can deploy addons by calling {@link Bootstrap#install(String)}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Bootstrap
{
   private static final String FORGE_ADDON_GROUP_ID = "org.jboss.forge.addon:";
   private final Furnace furnace;
   private boolean exitAfter = false;
   private boolean batchMode = false;

   public static void main(final String[] args) throws InterruptedException, ExecutionException
   {
      final List<String> bootstrapArgs = new ArrayList<>();
      final Properties systemProperties = System.getProperties();
      // Set system properties
      for (String arg : args)
      {
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

      // Check for the forge log directory
      final String logDir = systemProperties.getProperty("org.jboss.forge.log.file",
               new File(OperatingSystemUtils.getUserForgeDir(), "log/forge.log").getAbsolutePath());
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

   private Bootstrap(String[] args)
   {
      boolean listInstalled = false;
      String installAddon = null;
      String removeAddon = null;
      furnace = ServiceLoader.load(Furnace.class).iterator().next();

      furnace.setArgs(args);

      if (args.length > 0)
      {
         List<String> listArgs = Arrays.asList(args);
         if (listArgs.contains("--help") || listArgs.contains("-h"))
         {
            System.out.println(help());
            exitAfter = true;
            return;
         }
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
               System.out.println("Forge version " + AddonRepositoryImpl.getRuntimeAPIVersion());
               exitAfter = true;
            }
            else
            {
               System.out.println("forge: unrecognized option: '" + args[i] + "'");
               System.out.println("Try 'forge --help' for more information.");
               exitAfter = true;
            }
         }
      }

      if (!containsMutableRepository(furnace.getRepositories()))
      {
         furnace.addRepository(AddonRepositoryMode.MUTABLE, new File(OperatingSystemUtils.getUserForgeDir(), "addons"));
      }
      if (listInstalled)
      {
         list();
      }
      if (installAddon != null)
      {
         install(installAddon);
      }
      if (removeAddon != null)
      {
         remove(removeAddon);
      }
   }

   private String help()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("Usage: forge [OPTION]... PARAMETER ... \n");
      sb.append("The fastest way to build applications, share your software, and enjoy doing it. \n");
      sb.append("\n");
      sb.append("-i, --install [[groupId:]addon[,version]]\n");
      sb.append("\t install the required addons and exit. ex: `forge -i core-addon-x` or `forge -i org.example.addon:example,1.0.0` \n");

      sb.append("-r, --remove [[groupId:]addon[,version]]\n");
      sb.append("\t remove the required addons and exit. ex: `forge -r core-addon-x` or `forge -r org.example.addon:example,1.0.0` \n");

      sb.append("-l, --list\n");
      sb.append("\t list installed addons and exit \n");

      sb.append("-a, --addonDir [dir]\n");
      sb.append("\t add the given directory for use as a custom addon repository \n");

      sb.append("-e, --evaluate [cmd]\n");
      sb.append("\t evaluate the given string as commands (requires shell addon. Install via: `forge -i shell`) \n");

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

   private boolean containsMutableRepository(List<AddonRepository> repositories)
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

   private void list()
   {
      try
      {
         for (AddonRepository repository : furnace.getRepositories())
         {
            System.out.println(repository.getRootDirectory().getCanonicalPath() + ":");
            List<AddonId> addons = repository.listEnabled();
            for (AddonId addon : addons)
            {
               System.out.println(addon.toCoordinates());
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.out.println("> Forge version [" + AddonRepositoryImpl.getRuntimeAPIVersion() + "]");
      }
      finally
      {
         exitAfter = true;
      }
   }

   private List<AddonId> getEnabledAddonIds()
   {
      List<AddonId> result = new ArrayList<>();
      for (AddonRepository repository : furnace.getRepositories())
      {
         List<AddonId> addons = repository.listEnabled();
         result.addAll(addons);
      }
      return result;
   }

   private void start() throws InterruptedException, ExecutionException
   {
      if (!exitAfter)
      {
         if (!batchMode)
         {
            List<AddonId> addonIds = getEnabledAddonIds();
            if (addonIds.isEmpty())
            {
               String result = System.console().readLine(
                        "There are no addons installed; install core addons now? [Y,n] ");
               if (!"n".equalsIgnoreCase(result.trim()))
               {
                  install("core");
               }
            }
         }

         furnace.start();
      }
   }

   private void install(String addonCoordinates)
   {
      Version runtimeAPIVersion = AddonRepositoryImpl.getRuntimeAPIVersion();
      try
      {
         AddonDependencyResolver resolver = new MavenAddonDependencyResolver();
         AddonManagerImpl addonManager = new AddonManagerImpl(furnace, resolver);

         AddonId addon;
         // This allows forge --install maven
         if (addonCoordinates.contains(","))
         {
            if (addonCoordinates.contains(":"))
            {
               addon = AddonId.fromCoordinates(addonCoordinates);
            }
            else
            {
               addon = AddonId.fromCoordinates(FORGE_ADDON_GROUP_ID + addonCoordinates);
            }
         }
         else
         {
            AddonId[] versions;
            String coordinate;
            if (addonCoordinates.contains(":"))
            {
               coordinate = addonCoordinates;
               versions = resolver.resolveVersions(addonCoordinates).get();
            }
            else
            {
               coordinate = FORGE_ADDON_GROUP_ID + addonCoordinates;
               versions = resolver.resolveVersions(coordinate).get();
            }

            if (versions.length == 0)
            {
               throw new IllegalArgumentException("No Artifact version found for " + coordinate);
            }
            else
            {
               AddonId selected = null;
               for (int i = versions.length - 1; selected == null && i >= 0; i--)
               {
                  String apiVersion = resolver.resolveAPIVersion(versions[i]).get();
                  if (apiVersion != null
                           && Versions.isApiCompatible(runtimeAPIVersion, new SingleVersion(apiVersion)))
                  {
                     selected = versions[i];
                  }
               }
               if (selected == null)
               {
                  throw new IllegalArgumentException("No compatible addon API version found for " + coordinate
                           + " for API " + runtimeAPIVersion);
               }

               addon = selected;
            }
         }

         AddonActionRequest request = addonManager.install(addon);
         System.out.println(request);
         if (!batchMode)
         {
            String result = System.console().readLine("Confirm installation [Y/n]? ");
            if ("n".equalsIgnoreCase(result.trim()))
            {
               System.out.println("Installation aborted.");
               return;
            }
         }
         request.perform();
         System.out.println("Installation completed successfully.");
         System.out.println();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.out.println("> Forge version [" + runtimeAPIVersion + "]");
      }
      finally
      {
         exitAfter = true;
      }
   }

   private void remove(String addonCoordinates)
   {
      try
      {
         AddonDependencyResolver resolver = new MavenAddonDependencyResolver();
         AddonManagerImpl addonManager = new AddonManagerImpl(furnace, resolver);
         AddonId addon = null;
         String coordinates;
         // This allows forge --remove maven
         if (addonCoordinates.contains(","))
         {
            if (addonCoordinates.contains(":"))
            {
               addon = AddonId.fromCoordinates(addonCoordinates);
            }
            else
            {
               addon = AddonId.fromCoordinates(FORGE_ADDON_GROUP_ID + addonCoordinates);
            }
            coordinates = addon.getName();
         }
         else
         {
            if (addonCoordinates.contains(":"))
            {
               coordinates = addonCoordinates;
            }
            else
            {
               coordinates = FORGE_ADDON_GROUP_ID + addonCoordinates;
            }
         }
         REPOS: for (AddonRepository repository : furnace.getRepositories())
         {
            for (AddonId id : repository.listEnabled())
            {
               if (coordinates.equals(id.getName()))
               {
                  addon = id;
                  if (repository instanceof MutableAddonRepository)
                  {
                     RemoveRequest request = addonManager.remove(id, (repository));
                     System.out.println(request);
                     if (!batchMode)
                     {
                        String result = System.console().readLine("Confirm uninstallation [Y/n]? ");
                        if ("n".equalsIgnoreCase(result.trim()))
                        {
                           System.out.println("Uninstallation aborted.");
                           return;
                        }
                     }
                     request.perform();
                     System.out.println("Uninstallation completed successfully.");
                     System.out.println();
                  }
                  break REPOS;
               }
            }
         }
         if (addon == null)
         {
            throw new IllegalArgumentException("No addon exists with id " + coordinates);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.out.println("> Forge version [" + AddonRepositoryImpl.getRuntimeAPIVersion() + "]");
      }
      finally
      {
         exitAfter = true;
      }
   }

   private static String getServiceName(final ClassLoader classLoader, final String className)
   {
      try (final InputStream stream = classLoader.getResourceAsStream("META-INF/services/" + className))
      {
         if (stream != null)
         {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream)))
            {
               String line;
               while ((line = reader.readLine()) != null)
               {
                  final int i = line.indexOf('#');
                  if (i != -1)
                  {
                     line = line.substring(0, i);
                  }
                  line = line.trim();
                  if (line.length() == 0)
                  {
                     continue;
                  }
                  return line;
               }
            }
            catch (IOException e)
            {
               // ignore
            }
         }
      }
      catch (IOException e)
      {
         // ignore
      }
      return null;
   }
}
