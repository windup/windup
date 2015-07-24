package org.jboss.windup.ui;

import org.jboss.windup.exec.updater.RulesetsUpdater;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.event.PostStartup;


@Singleton
public class RulesetUpdateChecker
{
    @Inject
    Furnace furnace;

    //@Inject // FORGE-2408
    //RulesetsUpdater updater;

    public void perform(@Observes PostStartup event)
    {
        if (!event.getAddon().getId().getName().contains("org.jboss.windup.ui:windup-ui"))
            return;


        RulesetsUpdater updater = furnace.getAddonRegistry().getServices(RulesetsUpdater.class).get();

        if (!event.getAddon().getRepository().isDeployed(AddonId.from("org.jboss.windup.exec:windup-exec", event.getAddon().getId().getVersion())))
            throw new IllegalStateException("windup-exec is not deployed.");

        if (updater.rulesetsNeedUpdate())
        {
            System.out.println("\nThe rulesets are outdated: " + updater.getRulesetsDir()
                + "\nConsider running Windup with --updateRuleset or --updateDistribution. Press ENTER to continue.\n");
        }
    }

}
