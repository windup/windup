package org.jboss.windup.ui;


import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.windup.exec.updater.RulesetsUpdater;
import org.jboss.windup.util.Util;

/**
 * Provides a basic UI command updating the rules/migration-core folder with the latest version.
 *
 * @author mbriskar
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindupUpdateRulesetCommand implements UICommand
{

    @Inject
    private DependencyResolver dependencyResolver;

    @Inject
    private RulesetsUpdater updater;
    

    @Override
    public UICommandMetadata getMetadata(UIContext ctx)
    {
        return Metadata.forCommand(getClass()).name(Util.WINDUP_BRAND_NAME_ACRONYM+" Update Ruleset")
                    .description("Update the ruleset containing the migration rules")
                    .category(Categories.create("Platform", "Migration"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception
    {
        return Results.fail("Update was disabled!");
        /* temporary disabled update of rules due possible breakage on compatibility and proper
         * function
        String updatedTo = updater.replaceRulesetsDirectoryWithLatestReleaseIfAny();

        if (updatedTo == null)
            return Results.fail("The ruleset is already in the most updated version.");
        else
            return Results.success("Sucessfully updated the rulesets to version " + updatedTo + " .");
        */
    }


    @Override
    public boolean isEnabled(UIContext context)
    {
        //temporary disabled
        return false;
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception
    {

    }

    @Override
    public void validate(UIValidationContext context)
    {

    }
}
