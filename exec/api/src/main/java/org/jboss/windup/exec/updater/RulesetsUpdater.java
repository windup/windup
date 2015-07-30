package org.jboss.windup.exec.updater;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.DependencyException;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.maven.resources.MavenModelResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;

/**
 * Encloses the functionality of updating the rulesets.
 *
 * @author mbriskar
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class RulesetsUpdater
{
    private static final Logger log = Logger.getLogger( RulesetsUpdater.class.getName() );

    private static final String RULESETS_ARTIFACT_ID = "windup-rulesets";
    private static final String RULES_GROUP_ID = "org.jboss.windup.rules";
    public static final String RULESET_CORE_DIRECTORY = "migration-core";


    @Inject
    private Furnace furnace;

    @Inject
    private DependencyResolver depsResolver;

    @Inject
    ResourceFactory factory;


    public boolean rulesetsNeedUpdate()
    {
        Coordinate lastRelease = this.getLatestReleaseOf(RULES_GROUP_ID, RULESETS_ARTIFACT_ID);
        Path windupRulesDir = getRulesetsDir();
        Path coreRulesPomPath = windupRulesDir.resolve(RULESET_CORE_DIRECTORY + "/META-INF/maven/org.jboss.windup.rules/windup-rulesets/pom.xml");
        File pomXml = coreRulesPomPath.toFile();
        if (!pomXml.exists())
            return false;

        MavenModelResource pom = (MavenModelResource) factory.create(pomXml);
        SingleVersion installed = new SingleVersion(pom.getCurrentModel().getVersion());
        SingleVersion latest = new SingleVersion(lastRelease.getVersion());
        final String msg = "Core rulesets: Installed: " + installed + " Latest release: " + latest;
        log.info(msg);
        System.out.println(msg); // Print to both to have the info in the log too.
        return 0 > installed.compareTo(latest);
    }


    /**
     * @return The directory which this updater works with.
     */
    public Path getRulesetsDir()
    {
        Path windupRulesDir = PathUtil.getWindupRulesDir();
        return windupRulesDir;
    }


    public String replaceRulesetsDirectoryWithLatestReleaseIfAny() throws IOException, DependencyException
    {
        if (!this.rulesetsNeedUpdate())
            return null;

        Path windupRulesDir = getRulesetsDir();
        Path coreRulesetsDir = windupRulesDir.resolve(RULESET_CORE_DIRECTORY);

        Coordinate rulesetsCoord = getLatestReleaseOf("org.jboss.windup.rules", "windup-rulesets");
        if(rulesetsCoord == null)
            throw new WindupException("No Windup release found.");

        FileUtils.deleteDirectory(coreRulesetsDir.toFile());
        extractArtifact(rulesetsCoord, coreRulesetsDir.toFile());

        return rulesetsCoord.getVersion();
    }


    public void extractArtifact(Coordinate artifactCoords, File targetDir) throws IOException, DependencyException
    {
        final DependencyQueryBuilder query = DependencyQueryBuilder.create(artifactCoords);
        Dependency dependency = depsResolver.resolveArtifact(query);
        FileResource<?> artifact = dependency.getArtifact();
        ZipUtil.unzipToFolder(new File(artifact.getFullyQualifiedName()), targetDir);
    }

    /**
     * A convenience method.
     * @return Finds the latest non-SNAPSHOT of given artifact.
     */
    public Coordinate getLatestReleaseOf(String groupId, String artifactId)
    {
        final CoordinateBuilder coord = CoordinateBuilder.create()
                .setGroupId(groupId)
                .setArtifactId(artifactId);
        return getLatestReleaseOf(coord);
    }


    /**
     * @return Finds the latest non-SNAPSHOT of given artifact.
     */
    public Coordinate getLatestReleaseOf(final CoordinateBuilder coord)
    {
        List<Coordinate> availableVersions = depsResolver.resolveVersions(DependencyQueryBuilder.create(coord));

        // Find the latest non-SNAPSHOT version.
        for(int i = availableVersions.size()-1; i >= 0; i--)
        {
            if(!availableVersions.get(i).isSnapshot())
                return availableVersions.get(i);
        }
        return null;
    }

}
