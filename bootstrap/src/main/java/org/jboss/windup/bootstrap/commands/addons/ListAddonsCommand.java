package org.jboss.windup.bootstrap.commands.addons;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.impl.addons.AddonRepositoryImpl;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.windup.bootstrap.commands.AbstractListCommand;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ListAddonsCommand extends AbstractListCommand implements Command, FurnaceDependent
{
    private Furnace furnace;

    @Override
    public void setFurnace(Furnace furnace)
    {
        this.furnace = furnace;
    }

    @Override
    public CommandResult execute()
    {
        printValuesSorted("Enabled addons", getEnabledAddons());
        return CommandResult.EXIT;
    }

    private Set<String> getEnabledAddons()
    {
        Set<String> addons = new HashSet<>();
        try
        {
            for (AddonRepository repository : furnace.getRepositories())
            {
                System.out.println(repository.getRootDirectory().getCanonicalPath() + ":");
                List<AddonId> enabledAddons = repository.listEnabled();
                for (AddonId addon : enabledAddons)
                {
                    addons.add(addon.toCoordinates());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("> Forge version [" + AddonRepositoryImpl.getRuntimeAPIVersion() + "]");
        }
        return addons;
    }

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.POST_CONFIGURATION;
    }
}
