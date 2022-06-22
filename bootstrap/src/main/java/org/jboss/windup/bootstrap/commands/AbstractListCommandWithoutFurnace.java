package org.jboss.windup.bootstrap.commands;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.bootstrap.help.Help;

import java.util.*;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class AbstractListCommandWithoutFurnace implements Command {
    /**
     * Print the given values after displaying the provided message.
     */
    protected static void printValuesSorted(String message, Set<String> values) {
        System.out.println();
        System.out.println(message + ":");
        List<String> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        for (String value : sorted) {
            System.out.println("\t" + value);
        }
    }

    protected static Set<String> getOptionValuesFromHelp(String optionName) {
        Set<String> options = new HashSet<>();
        Help.load().getOptions().stream()
                .filter(opt -> opt.getName().equals(optionName))
                .forEach(optionDescription -> options.addAll(optionDescription.getAvailableOptions()));
        return options;
    }
}
