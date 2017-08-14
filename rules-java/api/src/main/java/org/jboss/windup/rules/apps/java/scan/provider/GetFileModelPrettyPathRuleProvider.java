package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.FinalizePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.reporting.freemarker.filepath.GetPrettyPathForFile;
import org.jboss.windup.rules.apps.java.scan.ast.WindupWildcardImportResolver;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@RuleMetadata(phase = FinalizePhase.class)
public class GetFileModelPrettyPathRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logging.get(GetFileModelPrettyPathRuleProvider.class);

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(FileModel.class))
                    .perform(new GetFileModelPrettyPathRuleProvider.GetPrettyPathIterationOperator()
                                .and(Commit.every(100))
                                .and(IterationProgress.monitoring("Index Java Source Files", 250)));
    }

    private final class GetPrettyPathIterationOperator extends AbstractIterationOperation<FileModel>
    {
        private GetPrettyPathIterationOperator()
        {
            super();
        }

        @Override
        public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
        {
            WindupWildcardImportResolver.setContext(event.getGraphContext());
            try
            {
                GraphContext graphContext = event.getGraphContext();

                WindupConfigurationModel configuration = new GraphService<>(graphContext,
                            WindupConfigurationModel.class)
                                        .getUnique();

                payload.setCachedPrettyPath(payload.getPrettyPathWithinProject(true));
                GetPrettyPathForFile.addPrettyPathToModel(payload);
            }
            finally
            {
                WindupWildcardImportResolver.setContext(null);
            }
        }

        @Override
        public String toString()
        {
            return "GetPrettyPathInformation";
        }
    }
}
