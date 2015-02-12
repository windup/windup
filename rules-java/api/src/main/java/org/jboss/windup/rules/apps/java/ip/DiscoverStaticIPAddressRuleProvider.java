package org.jboss.windup.rules.apps.java.ip;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRules;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.files.condition.FileContent;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Finds files that contain potential static IP addresses, determined by regular expression.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class DiscoverStaticIPAddressRuleProvider extends WindupRuleProvider
{
    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return MigrationRules.class;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    // for all files ending in java, properties, and xml,
                    // query for the regular expression {ip}
                    .when(FileContent.matches("{ip}").inFilesNamed("{*}.{type}"))
                    .perform(new AbstractIterationOperation<FileLocationModel>()
                    {
                        // when a result is found, create an inline hint.
                        // reference the inline hint with the static ip marker so that we can query for it
                        // in the static ip report.
                        public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload)
                        {
                            // for all file location models that match the regular expression in the where clause, add the IP Location Model to the
                            // graph
                            StaticIPLocationModel location = GraphService.addTypeToModel(event.getGraphContext(), payload,
                                        StaticIPLocationModel.class);

                            location.setTitle("Static IP: " + location.getSourceSnippit());
                            location.setHint("When migrating environments, static IP addresses may need to be modified or eliminated.");
                            location.setEffort(0);
                        };
                    })
                    .where("ip").matches("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b")
                    .where("type").matches("java|properties|xml");
    }

}
