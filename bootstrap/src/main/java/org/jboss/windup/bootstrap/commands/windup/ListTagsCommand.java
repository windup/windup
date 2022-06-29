package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.bootstrap.commands.AbstractListCommand;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.config.metadata.RuleProviderRegistryCache;
import org.jboss.windup.exec.configuration.options.IncludeTagsOption;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ListTagsCommand extends AbstractListCommand implements Command, FurnaceDependent {
    private final List<Path> userProvidedPaths;

    public ListTagsCommand(List<String> arguments) {
        userProvidedPaths = getUserProvidedPaths(arguments);
    }

    @Override
    public CommandResult execute() {
        Set<String> result;
        if (userProvidedPaths.isEmpty()) {
            result = getOptionValuesFromHelp(IncludeTagsOption.NAME);
        } else {
            RuleProviderRegistryCache ruleProviderRegistryCache = getFurnace().getAddonRegistry().getServices(RuleProviderRegistryCache.class).get();
            userProvidedPaths.forEach(userProvidedPath -> ruleProviderRegistryCache.addUserRulesPath(userProvidedPath));
            result = ruleProviderRegistryCache.getAvailableTags();
        }
        printValuesSorted("Available tags", result);
        return CommandResult.EXIT;
    }

    @Override
    public CommandPhase getPhase() {
        return userProvidedPaths.isEmpty() ? CommandPhase.PRE_CONFIGURATION : CommandPhase.PRE_EXECUTION;
    }

}
