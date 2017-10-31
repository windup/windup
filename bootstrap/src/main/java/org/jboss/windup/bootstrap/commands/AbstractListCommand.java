package org.jboss.windup.bootstrap.commands;

import org.jboss.forge.furnace.Furnace;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class AbstractListCommand extends AbstractListCommandWithoutFurnace implements Command, FurnaceDependent
{
    private Furnace furnace;

    protected Furnace getFurnace()
    {
        return furnace;
    }

    @Override
    public void setFurnace(Furnace furnace)
    {
        this.furnace = furnace;
    }
}
