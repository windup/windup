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
public class RulesetsUpdater {
    private static final Logger LOG = Logger.getLogger(RulesetsUpdater.class.getName());

    private static final String RULESETS_ARTIFACT_ID = "windup-rulesets";
    private static final String RULES_GROUP_ID = "org.jboss.windup.rules";
    public static final String RULESET_CORE_DIRECTORY = "migration-core";

    @Inject
    private Furnace furnace;

    @Inject
    private DependencyResolver depsResolver;

    @Inject
    ResourceFactory factory;


    // This may be called (and print the message) twice - first in RulesetUpdateChecker, then here.
    // TODO: This should be redesigned - first, do a query for current and latest version.
    //       Then use the result to print and to compare, as needed.
    public boolean rulesetsNeedUpdate(boolean printVersions) {
        return false;
        /* Temporary disabled
        SingleVersion latest =  getLatestCoreRulesetVersion();
        if (latest == null)
        {
            LOG.info("Could not query the latest core version.");
            return false;
        }

        SingleVersion installed = getCurrentCoreRulesetsVersion();
        if (installed == null)
        {
            LOG.warning("Could not detect the current core rulesets version.");
            return false;
        }

        if (printVersions)
            printRulesetsVersions(installed, latest);
        return 0 > installed.compareTo(latest);
        */
    }

    private SingleVersion getCurrentCoreRulesetsVersion() {
        Path windupRulesDir = getRulesetsDir();
        Path coreRulesPomPath = windupRulesDir.resolve(RULESET_CORE_DIRECTORY + "/META-INF/maven/org.jboss.windup.rules/windup-rulesets/pom.xml");
        File pomXml = coreRulesPomPath.toFile();
        if (!pomXml.exists())
            return null;
        MavenModelResource pom = (MavenModelResource) factory.create(pomXml);
        return SingleVersion.valueOf(pom.getCurrentModel().getVersion());
    }

    public void printRulesetsVersions(SingleVersion installed, SingleVersion latest) {
        final String msg = "Core rulesets version: Installed: " + installed + " Latest release: " + latest;
        LOG.info(msg);
        System.out.println(msg); // Print to both to have the info in the log too.
    }

    /**
     * @return The directory which this updater works with.
     */
    public Path getRulesetsDir() {
        Path windupRulesDir = PathUtil.getWindupRulesDir();
        return windupRulesDir;
    }


    public String replaceRulesetsDirectoryWithLatestReleaseIfAny() throws IOException, DependencyException {
        if (!this.rulesetsNeedUpdate(false))
            return null;

        Path windupRulesDir = getRulesetsDir();
        Path coreRulesetsDir = windupRulesDir.resolve(RULESET_CORE_DIRECTORY);

        Coordinate rulesetsCoord = getLatestReleaseOf(RULES_GROUP_ID, RULESETS_ARTIFACT_ID);
        if (rulesetsCoord == null)
            throw new WindupException("No Windup rulesets release found.");

        FileUtils.deleteDirectory(coreRulesetsDir.toFile());
        extractArtifact(rulesetsCoord, coreRulesetsDir.toFile());

        return rulesetsCoord.getVersion();
    }


    public void extractArtifact(Coordinate artifactCoords, File targetDir) throws IOException, DependencyException {
        final DependencyQueryBuilder query = DependencyQueryBuilder.create(artifactCoords);
        Dependency dependency = depsResolver.resolveArtifact(query);
        FileResource<?> artifact = dependency.getArtifact();
        ZipUtil.unzipToFolder(new File(artifact.getFullyQualifiedName()), targetDir);
    }

    /**
     * A convenience method.
     *
     * @return Finds the latest non-SNAPSHOT of given artifact.
     */
    public Coordinate getLatestReleaseOf(String groupId, String artifactId) {
        final CoordinateBuilder coord = CoordinateBuilder.create()
                .setGroupId(groupId)
                .setArtifactId(artifactId);
        return getLatestReleaseOf(coord);
    }


    /**
     * @return Finds the latest non-SNAPSHOT of given artifact.
     */
    public Coordinate getLatestReleaseOf(final CoordinateBuilder coord) {
        List<Coordinate> availableVersions = depsResolver.resolveVersions(DependencyQueryBuilder.create(coord));

        // Find the latest non-SNAPSHOT and non-CR version.
        for (int i = availableVersions.size() - 1; i >= 0; i--) {
            Coordinate availableCoord = availableVersions.get(i);
            String versionStr = availableCoord.getVersion();

            if (versionStr != null && !availableCoord.isSnapshot() && !versionStr.matches(".*CR[0-9]$"))
                return availableCoord;
        }
        return null;
    }


    /**
     * Connects to a repository and returns the version of the latest windup-distribution.
     */
    public Coordinate queryLatestWindupRelease() {
        final CoordinateBuilder coords = CoordinateBuilder.create()
                .setGroupId("org.jboss.windup")
                .setArtifactId("windup-distribution")
                .setClassifier("offline")
                .setPackaging("zip");
        Coordinate coord = this.getLatestReleaseOf(coords);
        return coord;
    }

    /**
     * @return The version of this running instance of Windup. Takes the windup-exec addon version as authoritative.
     */
    public String getCurrentRunningWindupVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    private SingleVersion getLatestCoreRulesetVersion() {
        Coordinate lastRelease = this.getLatestReleaseOf(RULES_GROUP_ID, RULESETS_ARTIFACT_ID);
        if (lastRelease == null)
            return null;
        return new SingleVersion(lastRelease.getVersion());
    }

}
