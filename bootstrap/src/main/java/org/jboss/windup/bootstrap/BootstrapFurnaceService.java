package org.jboss.windup.bootstrap;

import static org.jboss.windup.bootstrap.Bootstrap.getVersionString;

import java.util.ArrayList;
import java.util.List;
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
import org.jboss.forge.furnace.repositories.MutableAddonRepository;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.forge.furnace.versions.Versions;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class BootstrapFurnaceService
{

    private static final String FORGE_ADDON_GROUP_ID = "org.jboss.forge.addon:";

    private final Furnace furnace;

    public BootstrapFurnaceService(Furnace furnace)
    {
        this.furnace = furnace;
    }

    boolean list()
    {
        boolean exitAfter = true;
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
            exitAfter = false;
        }
        return exitAfter;
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

    /**
     * Install core addons if none are installed; then start.
     */
    void start(boolean exitAfter, boolean batchMode) throws InterruptedException, ExecutionException
    {
        if (exitAfter)
            return;

        if (!batchMode)
        {
            List<AddonId> addonIds = getEnabledAddonIds();
            if (addonIds.isEmpty())
            {
                String result = System.console().readLine(
                            "There are no addons installed; install core addons now? [Y,n] ");
                if (!"n".equalsIgnoreCase(result.trim()))
                {
                    install("core", batchMode);
                }
            }
        }

        furnace.start();
    }

    boolean install(String addonCoordinates, boolean batchMode)
    {
        Version runtimeAPIVersion = AddonRepositoryImpl.getRuntimeAPIVersion();
        try
        {
            AddonDependencyResolver resolver = new MavenAddonDependencyResolver();
            AddonManagerImpl addonManager = new AddonManagerImpl(furnace, resolver);

            AddonId addon;
            // This allows windup --install maven
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
                    return false;
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
        return true;
    }

    /**
     * @return Exit after execution of this.
     */
    boolean remove(String addonCoordinates, boolean batchMode)
    {
        try
        {
            AddonDependencyResolver resolver = new MavenAddonDependencyResolver();
            AddonManagerImpl addonManager = new AddonManagerImpl(furnace, resolver);
            AddonId addon = null;
            String coordinates;

            if (addonCoordinates.contains(":"))
                addonCoordinates = FORGE_ADDON_GROUP_ID + addonCoordinates;

            // This allows windup --remove maven
            if (addonCoordinates.contains(","))
            {
                addon = AddonId.fromCoordinates(addonCoordinates);
                coordinates = addon.getName();
            }
            else
            {
                coordinates = addonCoordinates;
            }

            REPOS: for (AddonRepository repository : furnace.getRepositories())
            {
                for (AddonId id : repository.listEnabled())
                {
                    if (!coordinates.equals(id.getName()))
                        continue;

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
                                return false;
                            }
                        }
                        request.perform();
                        System.out.println("Uninstallation completed successfully.");
                        System.out.println();
                    }
                    break REPOS;
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
            System.out.println(getVersionString());
        }
        return true;
    }

    Furnace getFurnace()
    {
        return this.furnace;
    }

}
