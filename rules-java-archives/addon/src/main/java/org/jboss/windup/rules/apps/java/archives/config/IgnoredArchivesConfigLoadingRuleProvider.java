package org.jboss.windup.rules.apps.java.archives.config;

import java.io.File;
import java.util.logging.Logger;

import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitializationPhase;
import org.jboss.windup.rules.apps.java.archives.ignore.SkippedArchives;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.file.FileSuffixPredicate;
import org.jboss.windup.util.file.FileVisit;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Loads configuration for {@link SkipArchivesRuleProvider}.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RuleMetadata(phase = InitializationPhase.class)
public class IgnoredArchivesConfigLoadingRuleProvider extends AbstractRuleProvider {
    private static final Logger log = Logging.get(IgnoredArchivesConfigLoadingRuleProvider.class);

    @Override
    public Configuration getConfiguration(final RuleLoaderContext ruleLoaderContext) {
        ConfigurationBuilder config = ConfigurationBuilder.begin();
        config.addRule().perform(new GraphOperation() {
            public void perform(GraphRewrite event, EvaluationContext evCtx) {
                Visitor<File> visitor = (file) -> {
                    try {
                        log.info("Loading archive identification metadata from [" + file.getAbsolutePath() + "]");
                        SkippedArchives.load(file);
                    } catch (Exception e) {
                        throw new WindupException("Failed to load metadata from file [" + file + "]", e);
                    }
                };

                FileSuffixPredicate predicate = new FileSuffixPredicate("\\.archive-ignore\\.txt");
                FileVisit.visit(PathUtil.getUserIgnoreDir().toFile(), predicate, visitor);
                FileVisit.visit(PathUtil.getWindupIgnoreDir().toFile(), predicate, visitor);
            }
        });
        return config;
    }
}
