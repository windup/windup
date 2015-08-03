package org.jboss.windup.bootstrap.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.forge.furnace.Furnace;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class AbstractListCommand implements Command, FurnaceDependent
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

    /**
     * Print the given values after displaying the provided message.
     */
    protected static void printValuesSorted(String message, Set<String> values)
    {
        System.out.println();
        System.out.println(message + ":");
        List<String> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        for (String value : sorted)
        {
            System.out.println("\t" + value);
        }
    }
}
