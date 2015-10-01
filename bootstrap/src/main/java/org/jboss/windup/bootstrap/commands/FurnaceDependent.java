package org.jboss.windup.bootstrap.commands;

import org.jboss.forge.furnace.Furnace;

/**
 * An object that must first be provided a running {@link Furnace} instance before it may be used.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface FurnaceDependent
{
    /**
     * Set the {@link Furnace} instance.
     */
    void setFurnace(Furnace furnace);
}
