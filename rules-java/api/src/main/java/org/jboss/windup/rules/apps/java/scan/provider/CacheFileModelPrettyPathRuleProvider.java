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
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Caches the relative paths of {@link FileModel}s in the graph.
 *
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@RuleMetadata(phase = FinalizePhase.class)
public class CacheFileModelPrettyPathRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logging.get(CacheFileModelPrettyPathRuleProvider.class);

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(FileModel.class))
                .perform(new CacheFileModelPrettyPathRuleProvider.GetPrettyPathIterationOperator()
                        .and(Commit.every(5000))
                        .and(IterationProgress.monitoring("Caching FileModel Path Information", 2000)));
    }

    private final class GetPrettyPathIterationOperator extends AbstractIterationOperation<FileModel> {
        private GetPrettyPathIterationOperator() {
            super();
        }

        @Override
        public void perform(GraphRewrite event, EvaluationContext context, FileModel payload) {
            try {
                addPrettyPathToModel(payload);
            } catch (Exception e) {
                LOG.warning(e.getMessage());
            }
        }

        /**
         * FIXME - Hacky, typecast filled code below:
         *  - This is necessary due to a frames bug:
         *      - https://issues.jboss.org/browse/WINDUP-1610
         */
        private void addPrettyPathToModel(FileModel fileModel) {
            if (fileModel instanceof JavaClassFileModel) {
                JavaClassFileModel jcfm = ((JavaClassFileModel) fileModel);
                jcfm.setCachedPrettyPath(jcfm.getPrettyPathWithinProject(true));
            } else if (fileModel instanceof JavaSourceFileModel) {
                JavaSourceFileModel jsfm = ((JavaSourceFileModel) fileModel);
                jsfm.setCachedPrettyPath(jsfm.getPrettyPathWithinProject(true));
            } else if (fileModel instanceof ReportResourceFileModel) {
                ReportResourceFileModel rrfm = (ReportResourceFileModel) fileModel;
                rrfm.setCachedPrettyPath(rrfm.getPrettyPathWithinProject(false));
            } else {
                fileModel.setCachedPrettyPath(fileModel.getPrettyPathWithinProject(false));
            }
        }

        @Override
        public String toString() {
            return "GetPrettyPathInformation";
        }
    }
}
