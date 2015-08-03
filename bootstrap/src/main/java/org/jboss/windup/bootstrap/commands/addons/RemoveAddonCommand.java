package org.jboss.windup.bootstrap.commands.addons;

import static org.jboss.windup.bootstrap.Bootstrap.getVersionString;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.manager.impl.AddonManagerImpl;
import org.jboss.forge.furnace.manager.maven.addon.MavenAddonDependencyResolver;
import org.jboss.forge.furnace.manager.request.RemoveRequest;
import org.jboss.forge.furnace.manager.spi.AddonDependencyResolver;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.forge.furnace.repositories.MutableAddonRepository;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class RemoveAddonCommand extends AbstractAddonCommand implements Command, FurnaceDependent
{
    private Furnace furnace;
    private String addonId;
    private AtomicBoolean batchMode;

    public RemoveAddonCommand(String addonId, AtomicBoolean batchMode)
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
        remove(addonId, batchMode.get());
        return CommandResult.CONTINUE;
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

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.POST_CONFIGURATION;
    }
}
