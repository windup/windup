package org.jboss.windup.bootstrap.commands;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.exec.configuration.options.UserRulesDirectoryOption;
import org.jboss.windup.util.PathUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class AbstractListCommand extends AbstractListCommandWithoutFurnace implements Command, FurnaceDependent {
    private Furnace furnace;

    protected Furnace getFurnace() {
        return furnace;
    }

    @Override
    public void setFurnace(Furnace furnace) {
        this.furnace = furnace;
    }

    protected List<Path> getUserProvidedPaths(List<String> arguments) {
        List<Path> userProvidedPaths = new ArrayList<>();
        boolean foundUserRulesDirectoryOption = false;
        for (int i = 0; i < arguments.size(); i++) {
            String argument = arguments.get(i);
            if (argument.equalsIgnoreCase("--" + UserRulesDirectoryOption.NAME)) {
                foundUserRulesDirectoryOption = true;
            } else if (foundUserRulesDirectoryOption) {
                if (argument.startsWith("-")) {
                    break;
                } else {
                    userProvidedPaths.add(Paths.get(argument));
                }
            }
        }
        Path userRulesDir = PathUtil.getUserRulesDir();
        try {
            // do not filter files contained in UserRulesDir to search for "rhamt.xml", "windup.xml" or "mta.xml"
            // as it do not filter in the case of userProvidedPaths.
            // It just adds the dir to be scanned by RuleProviderRegistryCache
            if (Files.list(userRulesDir).count() > 0) userProvidedPaths.add(userRulesDir);
        } catch (IOException ioe) {
            System.err.println("Warning: Unable to load rules from " + userRulesDir.toString() + " due to " + ioe.getMessage());
        }
        return userProvidedPaths;
    }
}
