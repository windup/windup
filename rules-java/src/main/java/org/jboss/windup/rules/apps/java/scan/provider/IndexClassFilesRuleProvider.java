package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.binary.DecompileArchivesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.operation.AddClassFileMetadata;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers .class files from the applications being analyzed.
 * 
 */
public class IndexClassFilesRuleProvider extends WindupRuleProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.INITIAL_ANALYSIS;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(UnzipArchivesToOutputRuleProvider.class);
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
    {
        return asClassList(DecompileArchivesRuleProvider.class);
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        // Periodically commit the graph, as otherwise we end up with a huge amount of uncommitted data
        //  (and very slow performance)
        AbstractIterationOperation<FileModel> commitPeriodically = new AbstractIterationOperation<FileModel>()
        {
            private int uncommittedIterations = 0;
            
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
            {
                uncommittedIterations++;
                if (uncommittedIterations > 10) {
                    event.getGraphContext().getGraph().getBaseGraph().commit();
                    uncommittedIterations = 0;
                }
            }
        };
        
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.find(FileModel.class)
                                .withProperty(FileModel.IS_DIRECTORY, false)
                                .withProperty(FileModel.FILE_PATH, QueryPropertyComparisonType.REGEX, ".*\\.class")
                    )
                    .perform(new AddClassFileMetadata().and(commitPeriodically));
    }
    // @formatter:on
}
