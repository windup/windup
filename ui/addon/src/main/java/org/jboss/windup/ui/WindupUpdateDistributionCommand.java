package org.jboss.windup.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import javax.inject.Inject;

import net.lingala.zip4j.core.ZipFile;

import org.apache.commons.io.FileUtils;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.windup.util.PathUtil;

/**
 * Provides a basic UI command updating the whole windup distribution.
 *
 * @author mbriskar
 */
public class WindupUpdateDistributionCommand implements UICommand
{

    @Inject
    Addon currentAddon;
    @Inject
    private DependencyResolver dependencyResolver;

    @Override
    public UICommandMetadata getMetadata(UIContext ctx)
    {
        return Metadata.forCommand(getClass()).name("Windup Update Distribution").description("Update the whole windup installation")
                    .category(Categories.create("Platform", "Migration"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception
    {
        if (!context.getPrompt().promptBoolean(
                    "Are you sure you want to continue? This command will delete current directories: addons, bin, lib, rules/migration-core"))
        {
            return Results.fail("Updating distribution was aborted.");

        }
        List<Coordinate> distributionVersions = dependencyResolver.resolveVersions(DependencyQueryBuilder.create(CoordinateBuilder.create()
                    .setGroupId("org.jboss.windup")
                    .setArtifactId("windup-distribution")));
        Coordinate latestDistributionCoordinate = distributionVersions.get(distributionVersions.size() - 1);
        int i = 0;
        do
        {
            i++;
            latestDistributionCoordinate = distributionVersions.get(distributionVersions.size() - i);
        }
        while (latestDistributionCoordinate.isSnapshot());
        Version latestVersion = new SingleVersion(latestDistributionCoordinate.getVersion());
        Version installedVersion = currentAddon.getId().getVersion();
        if (latestVersion.compareTo(installedVersion) <= 0)
        {
            return Results.fail("Windup is already in the most updated version.");
        }
        Path windupRulesDir = PathUtil.getWindupRulesDir();
        Path addonsDir = PathUtil.getWindupAddonsDir();
        Path binDir = PathUtil.getWindupHome().resolve(PathUtil.BINARY_DIRECTORY_NAME);
        Path libDir = PathUtil.getWindupHome().resolve( PathUtil.LIBRARY_DIRECTORY_NAME);
        Path coreRulesPropertiesPath = windupRulesDir.resolve(RulesetUpdateChecker.RULESET_CORE_DIRECTORY);

        deleteWholeDirectory(coreRulesPropertiesPath);
        deleteWholeDirectory(addonsDir);
        deleteWholeDirectory(libDir);
        deleteWholeDirectory(binDir);
        Dependency dependency = dependencyResolver.resolveArtifact(DependencyQueryBuilder.create(CoordinateBuilder.create()
                    .setGroupId(latestDistributionCoordinate.getGroupId()).setArtifactId(latestDistributionCoordinate.getArtifactId())
                    .setVersion(latestDistributionCoordinate.getVersion()).setClassifier("offline").setPackaging("zip")));
        FileResource<?> artifact = dependency.getArtifact();
        ZipFile zipFile = new ZipFile(artifact.getFullyQualifiedName());
        File tempFolder = OperatingSystemUtils.createTempDir();
        String tempFolderPath = tempFolder.getAbsolutePath();
        zipFile.extractAll(tempFolderPath);
        FilenameFilter windupUnzipped = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.contains("windup-distribution"))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        };
        File[] windupDistrubution = new File(tempFolderPath).listFiles(windupUnzipped);
        String distributionExctractedPath = windupDistrubution[0].getAbsolutePath();
        FileUtils.copyDirectory(new File(distributionExctractedPath + "/" + PathUtil.ADDONS_DIRECTORY_NAME), addonsDir.toFile());
        FileUtils.copyDirectory(new File(distributionExctractedPath + "/" + PathUtil.BINARY_DIRECTORY_NAME), binDir.toFile());
        FileUtils.copyDirectory(new File(distributionExctractedPath + "/" + PathUtil.LIBRARY_DIRECTORY_NAME), libDir.toFile());
        File downloadedMigrationCore = new File(distributionExctractedPath + "/" + PathUtil.RULES_DIRECTORY_NAME + "/"
                    + RulesetUpdateChecker.RULESET_CORE_DIRECTORY);
        if (downloadedMigrationCore.exists())
        {
            // copy from rules/migration-core to migration-core
            FileUtils.copyDirectory(downloadedMigrationCore, coreRulesPropertiesPath.toFile());
        }
        else
        {
            // copy from rules/ to migration-core
            // TODO: This else branch is just for testing purposes while the online version does not contain migration-core folder
            FileUtils.copyDirectory(new File(distributionExctractedPath + "/" + PathUtil.RULES_DIRECTORY_NAME), coreRulesPropertiesPath.toFile());
        }
        return Results.success("Sucessfully updated to version " + latestDistributionCoordinate.getVersion() + ". Please restart windup.");
    }

    private void deleteWholeDirectory(Path directory) throws IOException
    {
        if (directory.toFile().exists())
        {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException
                {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                            IOException exc) throws IOException
                {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Override
    public boolean isEnabled(UIContext context)
    {
        return true;
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception
    {

    }

    @Override
    public void validate(UIValidationContext context)
    {

    }

}
