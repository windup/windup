package org.jboss.windup.reporting.rules.rendering;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PreReportGenerationPhase;
import org.jboss.windup.config.tags.TagService;
import org.jboss.windup.config.tags.TagServiceHolder;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This renders the data of tags into a .js file, which is later linked from various report pages.
 *
 * @author Ondrej Zizka
 */
@RuleMetadata(phase = PreReportGenerationPhase.class, before = {RenderReportRuleProvider.class})
public class RenderTagsJavaScriptRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logging.get(RenderTagsJavaScriptRuleProvider.class);

    @Inject
    private Furnace furnace;

    @Inject
    private TagServiceHolder tagServiceHolder;

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder
                .begin()
                .addRule()
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        TagService tagService = tagServiceHolder.getTagService();

                        ReportService reportService = new ReportService(event.getGraphContext());
                        Path outputDir = reportService.getReportDirectory();
                        File tagsDataFile = outputDir.resolve("resources/tagsData.js").toFile();
                        try {
                            FileUtils.forceMkdir(tagsDataFile.getParentFile());
                        } catch (IOException ex) {
                            LOG.severe("Error creating a directory: " + tagsDataFile.getParentFile().getPath());
                            return;
                        }

                        try (FileWriter writer = new FileWriter(tagsDataFile)) {
                            tagService.writeTagsToJavaScript(writer);
                            LOG.info("Exporting tags data to file: " + tagsDataFile.getPath());
                        } catch (IOException e) {
                            LOG.severe("Error exporting tags data to: " + tagsDataFile.getPath());
                            return;
                        }
                    }
                });
    }
    // @formatter:on
}
