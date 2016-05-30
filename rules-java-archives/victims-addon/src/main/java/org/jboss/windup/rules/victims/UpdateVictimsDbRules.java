package org.jboss.windup.rules.victims;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitializationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.redhat.victims.VictimsException;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;

/**
 * Victims related rules: database update, archive hashes comparison.
 *
 * Currently this is done through the Victims API. However that's not too well implemented.
 * We could download the data, store it and simply use it through a HashMap.
 * http://www.victi.ms/service/v2/update/1900-05-26T16:28:26/
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@RuleMetadata(tags = {"java", "security"}, phase = InitializationPhase.class)
public class UpdateVictimsDbRules extends AbstractRuleProvider
{
    private static final Logger log = Logging.get(UpdateVictimsDbRules.class);

    // @formatter:off
    @Override
    public Configuration getConfiguration(final GraphContext context)
    {
        return ConfigurationBuilder.begin()

        // Update Victims DB.
        .addRule()
        // If not offline...
        .when(
            new GraphCondition()
            {
                public boolean evaluate(GraphRewrite event, EvaluationContext context)
                {
                    boolean offline = WindupConfigurationService.getConfigurationModel(event.getGraphContext()).isOfflineMode();
                    if (offline)
                        return false;

                    Boolean updateVictims = (Boolean) event.getGraphContext().getOptionMap().get(VictimsUpdateOption.NAME);
                    if (updateVictims == null || !updateVictims)
                        return false;

                    return true;
                }
            }
        )
        // ...then update.
        .perform(new GraphOperation()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context)
                {
                    try {
                        VictimsDBInterface db = VictimsDB.db();
                        // Update (goes to ~/.victims).
                        // That is configurable by sysprop victims.home .
                        db.synchronize();
                    }
                    catch(VictimsException ex){
                        log.log(Level.WARNING, "Failed updating Victims database: " + ex.getMessage(), ex);
                    }
                }
            }
        );
    }
    // @formatter:on
}
