package org.jboss.windup.bootstrap.commands.addons;

import java.io.File;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class AddImmutableAddonDirectoryCommand implements Command, FurnaceDependent
{
    private Furnace furnace;
    private String directory;

    public AddImmutableAddonDirectoryCommand(String directory)
    {
        this.directory = directory;
    }

    @Override
    public void setFurnace(Furnace furnace)
    {
        this.furnace = furnace;
    }

    @Override
    public CommandResult execute()
    {
        furnace.addRepository(AddonRepositoryMode.IMMUTABLE, new File(directory));
        return CommandResult.CONTINUE;
    }

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.CONFIGURATION;
    }

}
