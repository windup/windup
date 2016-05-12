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
 * Victi.ms related rules: database update, archive hashes comparison.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@RuleMetadata(tags = {"java"}, phase = InitializationPhase.class)
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
                    return !WindupConfigurationService.getConfigurationModel(event.getGraphContext()).isOfflineMode();
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
                        // Update (goes to ~/.victims)
                        db.synchronize();
                    }
                    catch(VictimsException ex){
                        log.log(Level.WARNING, "Failed updating Victi.ms database: " + ex.getMessage(), ex);
                    }
                }
            }
        );
    }
    // @formatter:on
}
