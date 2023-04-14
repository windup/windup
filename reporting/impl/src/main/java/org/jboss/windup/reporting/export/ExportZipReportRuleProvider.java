package org.jboss.windup.reporting.export;

import java.io.IOException;
import java.util.List;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PostFinalizePhase;
import org.jboss.windup.config.query.WindupConfigurationQuery;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates a ZIP file containing all the files in the output path
 *
 * @author Marco Rizzi
 */
@RuleMetadata(
        afterIDs = "DeleteWorkDirsAtTheEndRuleProvider",
        description = "Creates a ZIP file containing all the files in the output path."
                + " Use --" + WindupConfigurationModel.EXPORT_ZIP_REPORT + " to enable it.",
        phase = PostFinalizePhase.class
)
public class ExportZipReportRuleProvider extends AbstractRuleProvider {
    public static final String ZIP_REPORTS_NAME = "reports.zip";
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {

        return ConfigurationBuilder.begin()
                .addRule()
                .when(WindupConfigurationQuery.hasOption(WindupConfigurationModel.EXPORT_ZIP_REPORT, true).as("discard"))
                .perform(
                        new GraphOperation() {
                            public void perform(GraphRewrite event, EvaluationContext context) {
                                final WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
                                try {
                                    final FileModel outputFolderToZip = windupConfiguration.getOutputPath();
                                    ZipUtil.zipFolder(outputFolderToZip.asFile().toPath(), outputFolderToZip.getFilePath(), ZIP_REPORTS_NAME, List.of(GraphContextFactory.DEFAULT_GRAPH_SUBDIRECTORY, "logs"));
                                } catch (IOException e) {
                                    throw new WindupException(e);
                                }
                            }

                            public String toString() {
                                return "Create a ZIP file to collect the reports";
                            }
                        }
                );
    }

}
