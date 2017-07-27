package org.jboss.windup.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.DependencyException;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.exec.updater.RulesetsUpdater;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.Util;
import org.jboss.windup.util.exception.WindupException;

/**
 * Performs the update of Windup distribution, currently by replacing a Windup directory
 * with the content of a new Windup version offline distribution archive.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class DistributionUpdater
{
    private static final Logger log = Logger.getLogger( DistributionUpdater.class.getName() );

    @Inject
    private RulesetsUpdater updater;

    public void replaceWindupDirectoryWithLatestDistribution()
    {
        Coordinate coord = updater.queryLatestWindupRelease();
        if(coord == null)
            throw new WindupException("No "+ Util.WINDUP_BRAND_NAME_ACRONYM +" release found.");
        log.info("Latest "+ Util.WINDUP_BRAND_NAME_ACRONYM +" version available: " + coord.getVersion());
        log.fine("Latest "+ Util.WINDUP_BRAND_NAME_ACRONYM +" version available: " + coord);

        replaceWindupDirectoryWithDistribution(coord);
    }


    /**
     * Downloads the given artifact, extracts it and replaces current Windup directory (as given by PathUtil)
     * with the content from the artifact (assuming it really is a Windup distribution zip).
     */
    public void replaceWindupDirectoryWithDistribution(Coordinate distCoordinate)
            throws WindupException
    {
        try
        {
            final CoordinateBuilder coord = CoordinateBuilder.create(distCoordinate);
            File tempFolder = OperatingSystemUtils.createTempDir();
            this.updater.extractArtifact(coord, tempFolder);

            File newDistWindupDir = getWindupDistributionSubdir(tempFolder);

            if (null == newDistWindupDir)
                throw new WindupException("Distribution update failed: "
                    + "The distribution archive did not contain the windup-distribution-* directory: " + coord.toString());


            Path addonsDir = PathUtil.getWindupAddonsDir();
            Path binDir = PathUtil.getWindupHome().resolve(PathUtil.BINARY_DIRECTORY_NAME);
            Path libDir = PathUtil.getWindupHome().resolve(PathUtil.LIBRARY_DIRECTORY_NAME);

            FileUtils.deleteDirectory(addonsDir.toFile());
            FileUtils.deleteDirectory(libDir.toFile());
            FileUtils.deleteDirectory(binDir.toFile());

            FileUtils.moveDirectory(new File(newDistWindupDir, PathUtil.ADDONS_DIRECTORY_NAME), addonsDir.toFile());
            FileUtils.moveDirectory(new File(newDistWindupDir, PathUtil.BINARY_DIRECTORY_NAME), binDir.toFile());
            FileUtils.moveDirectory(new File(newDistWindupDir, PathUtil.LIBRARY_DIRECTORY_NAME), libDir.toFile());

            // Rulesets
            Path rulesDir = PathUtil.getWindupHome().resolve(PathUtil.RULES_DIRECTORY_NAME); //PathUtil.getWindupRulesDir();
            File coreDir = rulesDir.resolve(RulesetsUpdater.RULESET_CORE_DIRECTORY).toFile();

            // This is for testing purposes. The releases do not contain migration-core/ .
            if (coreDir.exists())
            {
                // migration-core/ exists -> Only replace that one.
                FileUtils.deleteDirectory(coreDir);
                final File coreDirNew = newDistWindupDir.toPath().resolve(PathUtil.RULES_DIRECTORY_NAME).resolve(RulesetsUpdater.RULESET_CORE_DIRECTORY).toFile();
                FileUtils.moveDirectory(coreDirNew, rulesDir.resolve(RulesetsUpdater.RULESET_CORE_DIRECTORY).toFile());
            }
            else
            {
                // Otherwise replace the whole rules directory (backing up the old one).
                final String newName = "rules-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                FileUtils.moveDirectory(rulesDir.toFile(), rulesDir.getParent().resolve(newName).toFile());
                FileUtils.moveDirectory(new File(newDistWindupDir, PathUtil.RULES_DIRECTORY_NAME), rulesDir.toFile());
            }

            FileUtils.deleteDirectory(tempFolder);
        }
        catch (IllegalStateException | DependencyException | IOException ex)
        {
            throw new WindupException("Distribution update failed: " + ex.getMessage(), ex);
        }
    }


    public static File getWindupDistributionSubdir(File tempFolder) throws WindupException
    {
        File[] matchingDirs = tempFolder.listFiles((FilenameFilter) FileFilterUtils.prefixFileFilter("windup-distribution-", IOCase.INSENSITIVE));
        if (matchingDirs.length == 0)
            return null;
        return matchingDirs[0];
    }

}
