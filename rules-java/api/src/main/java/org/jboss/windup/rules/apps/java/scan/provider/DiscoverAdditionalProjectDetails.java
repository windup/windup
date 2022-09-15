package org.jboss.windup.rules.apps.java.scan.provider;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.service.JarManifestService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = DiscoverProjectStructurePhase.class, after = DiscoverNonMavenArchiveProjectsRuleProvider.class)
public class DiscoverAdditionalProjectDetails extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(ArchiveModel.class))
                .perform(new AbstractIterationOperation<ArchiveModel>() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload) {
                        getAdditionalProjectDetails(event.getGraphContext(), payload);
                    }
                });
    }

    private void getAdditionalProjectDetails(GraphContext context, ArchiveModel archiveModel) {
        ProjectModel projectModel = archiveModel.getProjectModel();

        /*
         * This likely means that we were ignoring the project or did not analyze it for some other reason.
         *
         * Just skip it in this case
         */
        if (projectModel == null)
            return;

        JarManifestService service = new JarManifestService(context);
        Iterable<JarManifestModel> manifests = service.getManifestsByArchive(archiveModel);

        // get any properties from the manifest files that we don't already have from other sources (eg, maven pom)
        for (JarManifestModel manifest : manifests) {
            if (StringUtils.isNotBlank(manifest.getName())
                    && (projectModel.getName() == null || projectModel.getName().equals(archiveModel.getArchiveName()))) {
                projectModel.setName(manifest.getName());
            }

            if (StringUtils.isNotBlank(manifest.getVendor()) && StringUtils.isBlank(projectModel.getOrganization())) {
                projectModel.setOrganization(manifest.getVendor());
            }

            if (StringUtils.isNotBlank(manifest.getVersion()) && StringUtils.isBlank(projectModel.getVersion())) {
                projectModel.setVersion(manifest.getVersion());
            }

            if (StringUtils.isNotBlank(manifest.getDescription()) && StringUtils.isBlank(projectModel.getDescription())) {
                projectModel.setDescription(manifest.getDescription());
            }
        }
    }
}
