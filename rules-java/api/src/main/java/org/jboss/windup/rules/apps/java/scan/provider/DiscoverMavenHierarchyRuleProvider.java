package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.logging.Logger;

/**
 * Links the Maven artifact archives according to their hierarchy.
 */
@RuleMetadata(phase = DiscoverProjectStructurePhase.class, after = {
        DiscoverMavenProjectsRuleProvider.class,
        DiscoverNonMavenArchiveProjectsRuleProvider.class,
        DiscoverNonMavenSourceProjectsRuleProvider.class})
public class DiscoverMavenHierarchyRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logging.get(DiscoverMavenProjectsRuleProvider.class);

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        AbstractIterationOperation<MavenProjectModel> setupParentModule = new AbstractIterationOperation<MavenProjectModel>() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, MavenProjectModel payload) {
                setMavenParentProject(payload);
            }

            @Override
            public String toString() {
                return "ConfigureProjectHierarchy";
            }
        };

        // @formatter:off
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(MavenProjectModel.class))
                .perform(setupParentModule);
        // @formatter:on
    }

    private void setParentProject(ArchiveModel archiveModel, MavenProjectModel projectModel) {
        if (archiveModel == null)
            return;

        if (archiveModel.getProjectModel() != null) {
            String mavenGAV = projectModel.getGroupId() + ":" + projectModel.getArtifactId() + ":"
                    + projectModel.getVersion();
            String archivePath = archiveModel.getFilePath();
            LOG.info("Setting parent project for: " + mavenGAV + " to: " + archivePath);
            projectModel.setParentProject(archiveModel.getProjectModel());
        } else {
            setParentProject(archiveModel.getParentArchive(), projectModel);
        }
    }

    private void setParentProject(FileModel fileModel, MavenProjectModel projectModel) {
        if (fileModel == null)
            return;

        else if (fileModel.getProjectModel() != null) {
            projectModel.setParentProject(fileModel.getProjectModel());
        } else {
            setParentProject(fileModel.getParentFile(), projectModel);
        }
    }

    private void setMavenParentProject(MavenProjectModel projectModel) {
        FileModel fileModel = projectModel.getRootFileModel();

        // skip if no file was discovered for it
        if (fileModel == null)
            return;

        if (fileModel instanceof ArchiveModel) {
            ArchiveModel archiveModel = (ArchiveModel) fileModel;
            // look at the parent archive first
            setParentProject(archiveModel.getParentArchive(), projectModel);
        } else {
            FileModel parentFile = fileModel.getParentFile();
            setParentProject(parentFile, projectModel);
        }
    }
}
