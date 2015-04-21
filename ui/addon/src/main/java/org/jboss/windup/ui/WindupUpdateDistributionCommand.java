package org.jboss.windup.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
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
 * Provides a basic UI command updating the whole windup distribution. This command provides only the first step of 2-step update that
 * consists of downloading the latest distribution version and saving it
 * into $WINDUP_HOME/.update. This is because deletion of files that are already on classpath is not supported in Windows (files are locked).
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
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
                    "Are you sure you want to continue? This command will delete current directories preserving only the extra added rules"))
        {
            return Results.fail("Updating distribution was aborted.");
        }
        List<Coordinate> distributionVersions = dependencyResolver.resolveVersions(DependencyQueryBuilder.create(CoordinateBuilder.create()
                    .setGroupId("org.jboss.windup")
                    .setArtifactId("windup-distribution")));
        Coordinate latestDistributionCoordinate ;
        int i = 0;
        do
        {
            i++;
            if(distributionVersions.size() <= i) {
                //if we haven't found final version
                return Results.fail("Have not found any online Final version of the distribution.");
            }
            latestDistributionCoordinate = distributionVersions.get(distributionVersions.size() - i);
        }
        while (latestDistributionCoordinate.isSnapshot());
        Version latestVersion = new SingleVersion(latestDistributionCoordinate.getVersion());
        Version installedVersion = currentAddon.getId().getVersion();
        if (latestVersion.compareTo(installedVersion) <= 0)
        {
            return Results.fail("Windup is already in the most updated version.");
        }
        Path updateDir = PathUtil.getWindupUpdateDir();
        
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
        FileUtils.copyDirectory(new File(distributionExctractedPath), updateDir.toFile());
        return Results.success("Sucessfully prepared to be updated to version " + latestDistributionCoordinate.getVersion() + ". Please restart windup in order to take effect.");
    }


    @Override
    public boolean isEnabled(UIContext context)
    {
        if(context.getProvider().isGUI()) {
            //user should update the whole IDE plugin instead
            return false;
        } else {
            return true;
        }
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
