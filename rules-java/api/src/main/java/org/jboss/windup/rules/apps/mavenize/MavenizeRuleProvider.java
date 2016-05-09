package org.jboss.windup.rules.apps.mavenize;

import java.util.Map;
import java.util.logging.Logger;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ArchiveExtractionPhase;
import org.jboss.windup.config.phase.ArchiveMetadataExtractionPhase;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.archives.config.ArchiveIdentificationConfigLoadingRuleProvider;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.condition.SourceMode;
import org.jboss.windup.rules.apps.java.scan.provider.DiscoverMavenHierarchyRuleProvider;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * Creates a stub of Maven project structure, including pom.xml's and the proper directory structure and dependencies,
 * based on the project structure determined by prior Windup rules (nested deployments) and the libraries included in them.
 *
 * TODO: For nested deployments like EAR with a WAR and JAR, this also creates the appropriate structure and packaging.
 *
 *  @author Ondrej Zizka, zizka at seznam.cz
 */
@RuleMetadata(after = {
    ArchiveMetadataExtractionPhase.class,
    ArchiveIdentificationConfigLoadingRuleProvider.class,
    ArchiveExtractionPhase.class,
    DiscoverMavenHierarchyRuleProvider.class,
    DiscoverProjectStructurePhase.class
}, phase = DependentPhase.class)
public class MavenizeRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logging.get(MavenizeRuleProvider.class);
    public static final MavenCoord JBOSS_PARENT = new MavenCoord("org.jboss", "jboss-parent", "20");
    public static final MavenCoord JBOSS_BOM_JAVAEE6_WITH_ALL = new MavenCoord("org.jboss.bom", "jboss-javaee-6.0-with-all", "1.0.7.Final");
    public static final MavenCoord JBOSS_BOM_JAVAEE7_WITH_ALL = new MavenCoord("org.jboss.bom", "wildfly-javaee7-with-tools", "10.0.1.Final");

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext grCtx)
    {
        ConditionBuilder applicationProjectModels = Query.fromType(WindupConfigurationModel.class);

        return ConfigurationBuilder.begin()
        // Create the BOM frame
        .addRule()
        .perform(new GraphOperation() {
            public void perform(GraphRewrite event, EvaluationContext context) {
                GlobalBomModel bom = event.getGraphContext().getFramed().addVertex(null, GlobalBomModel.class);
                ArchiveCoordinateModel jbossParent = event.getGraphContext().getFramed().addVertex(null, ArchiveCoordinateModel.class);
                copyTo(JBOSS_PARENT, jbossParent);
                bom.setParent(jbossParent);
            }
        })
        .withId("Mavenize-BOM-data-collection")

        // For each IdentifiedArchive, add it to the global BOM.
        .addRule()
        .when(Query.fromType(IdentifiedArchiveModel.class))
        .perform(new MavenizePutNewerVersionToGlobalBomOperation())
        .withId("Mavenize-BOM-file-creation")

        // For each application given to Windup as input, mavenize it.
        .addRule()
        .when(applicationProjectModels, SourceMode.isDisabled())
        .perform(new MavenizeApplicationOperation())
        .withId("Mavenize-projects-mavenization")
        ;
    }
    // @formatter:on


    /**
     * This operation puts the given IdentifiedArchiveModel to the global BOM frame.
     * If there's already one of such G:A:P, then the newer version is used.
     * Eventual version collisions are overridden in pom.xml's.
     */
    class MavenizePutNewerVersionToGlobalBomOperation extends AbstractIterationOperation<IdentifiedArchiveModel>
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, IdentifiedArchiveModel archive)
        {
            LOG.info("Adding to global BOM: " + archive.getCoordinate().toPrettyString());
            // BOM
            //Query.fromType(GlobalBomModel.class).getUnique();
            GraphService<GlobalBomModel> bomServ = new GraphService<>(event.getGraphContext(), GlobalBomModel.class);
            GlobalBomModel bom = bomServ.getUnique();

            // Check for an existing coord, add the new one
            /*GraphService<ArchiveCoordinateModel> coordServ = new GraphService<>(event.getGraphContext(), ArchiveCoordinateModel.class);
            Iterable<ArchiveCoordinateModel> coordsOfGivenGA = coordServ.findAllByProperties(
                    new String[]{ArchiveCoordinateModel.GROUP_ID, ArchiveCoordinateModel.ARTIFACT_ID},
                    new String[]{archive.getCoordinate().getGroupId(), archive.getCoordinate().getArtifactId()}
            );*/
            bom.addNewerDependency(archive.getCoordinate());
            // TODO
        }
    }

    /**
     * Create a stub of Maven project structure, including pom.xml's and the proper directory structure and dependencies,
     * based on the project structure determined by prior Windup rules (nested deployments) and the libraries included in them.
     */
    private class MavenizeApplicationOperation extends AbstractIterationOperation<WindupConfigurationModel>
    {
        public MavenizeApplicationOperation()
        {
        }

        @Override
        public void perform(GraphRewrite event, EvaluationContext evalContext, WindupConfigurationModel config)
        {
            Map<String, Object> options = event.getGraphContext().getOptionMap();
            if (Boolean.FALSE.equals((Boolean) options.get(MavenizeOption.NAME)))
                return;

            for (FileModel inputPath : config.getInputPaths())
            {
                ProjectModel projectModel = inputPath.getProjectModel();
                if (projectModel == null)
                    throw new WindupException("Error, no project found in: " + inputPath.getFilePath());

                new MavenizationService(event.getGraphContext()).mavenizeApp(projectModel);
            }
        }
    }


    private static void copyTo(MavenCoord from, ArchiveCoordinateModel to)
    {
        to.setArtifactId(from.getArtifactId());
        to.setGroupId(from.getGroupId());
        to.setVersion(from.getVersion());
        to.setClassifier(from.getClassifier());
        to.setPackaging(from.getPackaging());
    }

}
