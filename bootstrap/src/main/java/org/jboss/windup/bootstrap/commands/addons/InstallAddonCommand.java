package org.jboss.windup.bootstrap.commands.addons;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.impl.addons.AddonRepositoryImpl;
import org.jboss.forge.furnace.manager.impl.AddonManagerImpl;
import org.jboss.forge.furnace.manager.maven.addon.MavenAddonDependencyResolver;
import org.jboss.forge.furnace.manager.request.AddonActionRequest;
import org.jboss.forge.furnace.manager.spi.AddonDependencyResolver;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.forge.furnace.versions.Versions;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class InstallAddonCommand extends AbstractAddonCommand implements Command, FurnaceDependent
{
    private Furnace furnace;
    private String addonId;
    private AtomicBoolean batchMode;

    public InstallAddonCommand(String addonId, AtomicBoolean batchMode)
    {
        this.addonId = addonId;
        this.batchMode = batchMode;
    }

    @Override
    public void setFurnace(Furnace furnace)
    {
        this.furnace = furnace;
    }

    @Override
    public CommandResult execute()
    {
        install(addonId, batchMode.get());
        return CommandResult.CONTINUE;
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

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.POST_CONFIGURATION;
    }
}
