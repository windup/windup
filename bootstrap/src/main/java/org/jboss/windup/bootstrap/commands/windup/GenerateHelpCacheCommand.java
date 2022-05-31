package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.bootstrap.help.Help;

import java.io.IOException;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GenerateHelpCacheCommand implements Command, FurnaceDependent {
    private Furnace furnace;

    @Override
    public void setFurnace(Furnace furnace) {
        this.furnace = furnace;
    }

    @Override
    public CommandPhase getPhase() {
        return CommandPhase.PRE_EXECUTION;
    }

    @Override
    public CommandResult execute() {
        try {
            Help.save(furnace);
        } catch (IOException e) {
            System.err.println("ERROR: Could not generate completion data due to: " + e.getMessage());
            e.printStackTrace();
        }
        return CommandResult.CONTINUE;
    }
}
