package org.jboss.windup.rules.apps.java.archives.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitializationPhase;
import org.jboss.windup.maven.nexusindexer.client.TextFileToLucene7Converter;
import org.jboss.windup.rules.apps.java.archives.identify.CompositeArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.identify.InMemoryArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.identify.LuceneArchiveIdentificationService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.file.FileSuffixPredicate;
import org.jboss.windup.util.file.FileVisit;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * Loads configuration/metadata for identifying archives by SHA1 hashes.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RuleMetadata(phase = InitializationPhase.class)
public class ArchiveIdentificationConfigLoadingRuleProvider extends AbstractRuleProvider {
    private static final Logger log = Logging.get(ArchiveIdentificationConfigLoadingRuleProvider.class);

    @Inject
    private CompositeArchiveIdentificationService identifier;

    @Override
    public Configuration getConfiguration(final RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new AddDelimitedFileIndexOperation())
                .addRule()
                .perform(new CreateLuceneIndexOperation())
                .addRule()
                .perform(new AddLuceneFileIndexOperation());
    }

    private class AddDelimitedFileIndexOperation extends GraphOperation {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context) {
            Visitor<File> visitor = file -> {
                try {
                    log.info("Loading archive identification data from [" + file.getAbsolutePath() + "]");
                    identifier.addIdentifier(new InMemoryArchiveIdentificationService().addMappingsFrom(file));
                } catch (Exception e) {
                    throw new WindupException("Failed to load identification data from file [" + file + "]", e);
                }
            };

            FileSuffixPredicate predicate = new FileSuffixPredicate("\\.archive-metadata\\.txt");
            FileVisit.visit(PathUtil.getUserCacheDir().resolve("nexus-indexer-data").toFile(), predicate, visitor);
            FileVisit.visit(PathUtil.getWindupCacheDir().resolve("nexus-indexer-data").toFile(), predicate, visitor);
        }
    }

    private class AddLuceneFileIndexOperation extends GraphOperation {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context) {
            Visitor<File> visitor = file -> {
                try {
                    log.info("Loading archive identification data from [" + file.getAbsolutePath() + "]");
                    identifier.addIdentifier(new LuceneArchiveIdentificationService(file.getParentFile()));
                } catch (Exception e) {
                    throw new WindupException("Failed to load identification data from file [" + file + "]", e);
                }
            };

            FileSuffixPredicate predicate = new FileSuffixPredicate("archive-metadata\\.lucene\\.marker");
            FileVisit.visit(PathUtil.getUserCacheDir().resolve("nexus-indexer-data").toFile(), predicate, visitor);
            FileVisit.visit(PathUtil.getWindupCacheDir().resolve("nexus-indexer-data").toFile(), predicate, visitor);
        }
    }

    /**
     * Creates the lucene index from the text files if it is not already present.
     */
    private class CreateLuceneIndexOperation implements Operation {
        @Override
        public void perform(Rewrite event, EvaluationContext context) {
            // If nexus-indexer-data does not exist, it's the no-index distribution - skip
            Path indexDir = PathUtil.getWindupCacheDir().resolve("nexus-indexer-data");
            if (!Files.exists(indexDir)) {
                return;
            }

            // Check if the Lucene index already exists. If not, create it.
            Path luceneIndexDir = PathUtil.getWindupCacheDir().resolve("nexus-indexer-data/lucene");
            if (!Files.exists(luceneIndexDir)) {
                PathUtil.createDirectory(luceneIndexDir, "Lucene index dir");

                File inputFile = PathUtil.getWindupCacheDir().resolve("nexus-indexer-data/text").toFile();
                File outputDir = luceneIndexDir.toFile();

                File[] childFiles = inputFile.listFiles();
                if (childFiles == null)
                    throw new RuntimeException("No files in input directory: " + inputFile);

                String[] args = { inputFile.getAbsolutePath(), outputDir.getAbsolutePath() };
                try {
                    log.info("Lucene index not found");
                    log.info("Creating Lucene index. This may take a while...");
                    TextFileToLucene7Converter.main(args);
                    log.info("Lucene index successfully created!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
