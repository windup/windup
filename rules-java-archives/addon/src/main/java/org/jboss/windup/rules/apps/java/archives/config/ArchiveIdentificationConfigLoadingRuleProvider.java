package org.jboss.windup.rules.apps.java.archives.config;

import java.io.File;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitializationPhase;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.archives.identify.CompositeChecksumIdentifier;
import org.jboss.windup.rules.apps.java.archives.identify.SortedFileChecksumIdentifier;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.WindupPathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.file.FileSuffixPredicate;
import org.jboss.windup.util.file.FileVisit;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Loads configuration/metadata for {@link ArchiveIdentificationRuleProvider}.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ArchiveIdentificationConfigLoadingRuleProvider extends AbstractRuleProvider
{
    private static final Logger log = Logging.get(ArchiveIdentificationConfigLoadingRuleProvider.class);

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return InitializationPhase.class;
    }

    @Inject
    private CompositeChecksumIdentifier identifier;

    @Override
    public Configuration getConfiguration(final GraphContext grCtx)
    {
        ConfigurationBuilder config = ConfigurationBuilder.begin();
        config.addRule().perform(new GraphOperation()
        {
            public void perform(GraphRewrite event, EvaluationContext evCtx)
            {
                Visitor<File> visitor = new Visitor<File>()
                {
                    @Override
                    public void visit(File file)
                    {
                        try
                        {
                            log.info("Loading archive identification data from [" + file.getAbsolutePath() + "]");
                            identifier.addIdentifier(new SortedFileChecksumIdentifier(file));
                        }
                        catch (Exception e)
                        {
                            throw new WindupException("Failed to load identification data from file [" + file + "]", e);
                        }
                    }
                };

                FileSuffixPredicate predicate = new FileSuffixPredicate("\\.archive-metadata\\.txt");
                FileVisit.visit(WindupPathUtil.getUserCacheDir().resolve("nexus-indexer-data").toFile(), predicate, visitor);
                FileVisit.visit(WindupPathUtil.getWindupCacheDir().resolve("nexus-indexer-data").toFile(), predicate, visitor);
            }
        });
        return config;
    }
}
