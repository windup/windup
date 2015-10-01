package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.exec.updater.RulesetsUpdater;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class UpdateRulesetsCommand implements Command, FurnaceDependent
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
        System.out.println("Checking for rulesets updates...");
        try
        {
            RulesetsUpdater updater = furnace.getAddonRegistry().getServices(RulesetsUpdater.class).get();
            String newVersion = updater.replaceRulesetsDirectoryWithLatestReleaseIfAny();
            if (newVersion != null)
                System.out.println("Updated the rulesets to version " + newVersion + ".");
        }
        catch (Exception e)
        {
            System.err.println("Could not update the rulesets: " + e.getMessage());
            e.printStackTrace(System.err);
            return CommandResult.EXIT;
        }
        return CommandResult.CONTINUE;
    }

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.PRE_EXECUTION;
    }
}
