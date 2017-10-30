package org.jboss.windup.bootstrap.commands;

import java.util.*;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.bootstrap.help.Help;

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

    protected static Set<String> getOptionValuesFromHelp(String optionName)
    {
        Set<String> options = new HashSet<>();
        Help.load().getOptions().stream()
                .filter(opt -> opt.getName().equals(optionName))
                .forEach(optionDescription -> options.addAll(optionDescription.getAvailableOptions()));
        return options;
    }
}
