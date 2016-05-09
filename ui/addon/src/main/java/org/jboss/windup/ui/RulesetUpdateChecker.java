package org.jboss.windup.ui;

import org.jboss.windup.exec.updater.RulesetsUpdater;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.event.PostStartup;


/**
 * Upon the <code>PostStartup</code> CDI event, this checks whether
 * a Windup core ruleset update is available.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Singleton
public class RulesetUpdateChecker
{
    @Inject
    Furnace furnace;

    public void perform(@Observes PostStartup event)
    {
        if (!event.getAddon().getId().getName().contains("org.jboss.windup.ui:windup-ui"))
            return;

        final AddonId windupExec = AddonId.from("org.jboss.windup.exec:windup-exec", event.getAddon().getId().getVersion());
        if (!event.getAddon().getRepository().isDeployed(windupExec))
            throw new IllegalStateException("windup-exec is not deployed.");

        RulesetsUpdater updater = furnace.getAddonRegistry().getServices(RulesetsUpdater.class).get();
        if (updater.rulesetsNeedUpdate(true))
        {
            System.out.println("\nThe rulesets are outdated: " + updater.getRulesetsDir()
                + "\nConsider running Windup with --updateRulesets.\n");
        }
    }

}
