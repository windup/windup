package org.jboss.windup.bootstrap;

import static org.jboss.windup.bootstrap.Bootstrap.getVersionString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class BootstrapFurnaceService
{
    private static final String artifactWithColonVersionPattern = "(.*?):(.*?):(.*)";
    private static final String artifactWithCommaVersionPattern = "(.*?):(.*?),(.*)";
    private static final String artifactPattern = "(.*?):(.*?)";

    private final Furnace furnace;

    public BootstrapFurnaceService(Furnace furnace)
    {
        this.furnace = furnace;
    }

    void list()
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
    Future<Furnace> start(boolean batchMode) throws InterruptedException, ExecutionException
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
                    install("core", batchMode);
                }
            }
        }
        return furnace.startAsync();
    }

    void install(String coordinates, boolean batchMode)
    {
        Version runtimeAPIVersion = AddonRepositoryImpl.getRuntimeAPIVersion();
        try
        {
            AddonDependencyResolver resolver = new MavenAddonDependencyResolver();
            AddonManagerImpl addonManager = new AddonManagerImpl(furnace, resolver);

            AddonId addonId;

            coordinates = convertColonVersionToComma(coordinates);

            // This allows windup --install maven
            if (coordinates.matches(artifactWithCommaVersionPattern))
            {
                addonId = AddonId.fromCoordinates(coordinates);
            }
            else if (coordinates.matches(artifactPattern))
            {
                AddonId[] versions = resolver.resolveVersions(coordinates).get();
                String coordinate = coordinates;

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

                    addonId = selected;
                }
            }
            else
            {
                throw new IllegalArgumentException("Unrecognized format: " + coordinates + ", format must match: GROUP_ID:ARTIFACT_ID:VERSION");
            }

            AddonActionRequest request = addonManager.install(addonId);
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
        return;
    }

    /**
     * @return Exit after execution of this.
     */
    void remove(String coordinates, boolean batchMode)
    {
        try
        {
            AddonDependencyResolver resolver = new MavenAddonDependencyResolver();
            AddonManagerImpl addonManager = new AddonManagerImpl(furnace, resolver);
            AddonId addon = null;

            coordinates = convertColonVersionToComma(coordinates);

            // This allows windup --remove maven
            if (coordinates.matches(artifactWithCommaVersionPattern))
            {
                addon = AddonId.fromCoordinates(coordinates);
                coordinates = addon.getName();
            }

            REPOS: for (AddonRepository repository : furnace.getRepositories())
            {
                for (AddonId id : repository.listEnabled())
                {
                    if (!coordinates.equals(id.getName()) || !(repository instanceof MutableAddonRepository))
                        continue;

                    addon = id;
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
        return;
    }

    private String convertColonVersionToComma(String coordinates)
    {
        String result;
        Matcher matcher = Pattern.compile(artifactWithColonVersionPattern).matcher(coordinates);
        if (matcher.matches())
        {
            result = matcher.group(1) + ":" + matcher.group(2) + "," + matcher.group(3);
        }
        else
        {
            result = coordinates;
        }
        System.out.println("In: " + coordinates + ", Out: " + result);
        return result;
    }

    Furnace getFurnace()
    {
        return this.furnace;
    }

}
