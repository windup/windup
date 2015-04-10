package org.jboss.windup.ui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;

import net.lingala.zip4j.core.ZipFile;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.UIProvider;
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
import org.jboss.windup.util.PathUtil;

/**
 * Provides a basic Forge UI implementation for running Windup from within a {@link UIProvider}.
 *
 * @author jsightler <jesse.sightler@gmail.com>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WindupUpdateRulesetCommand implements UICommand
{

    @Inject
    private DependencyResolver dependencyResolver;

    @Override
    public UICommandMetadata getMetadata(UIContext ctx)
    {
        return Metadata.forCommand(getClass()).name("Windup Update Ruleset").description("Update the ruleset containing the migration rules")
                    .category(Categories.create("Platform", "Migration"));
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception
    {
        if (!RulesetUpdateChecker.rulesetNeedUpdate(dependencyResolver))
        {
            return Results.fail("The ruleset is already in the most updated version.");
        }
        List<Coordinate> resolveVersions = dependencyResolver.resolveVersions(DependencyQueryBuilder.create(CoordinateBuilder.create()
                    .setGroupId("org.jboss.windup.rules")
                    .setArtifactId("windup-rulesets")));
        Coordinate latestCoordinate = resolveVersions.get(resolveVersions.size() - 1);
        Path windupRulesDir = PathUtil.getWindupRulesDir();
        Path coreRulesPropertiesPath = windupRulesDir.resolve(RulesetUpdateChecker.RULESET_CORE_DIRECTORY);
        // delete the previous rules
        Files.walkFileTree(coreRulesPropertiesPath, new SimpleFileVisitor<Path>()
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
        Dependency dependency = dependencyResolver.resolveArtifact(DependencyQueryBuilder.create(CoordinateBuilder.create()
                    .setGroupId(latestCoordinate.getGroupId()).setArtifactId(latestCoordinate.getArtifactId())
                    .setVersion(latestCoordinate.getVersion())));
        FileResource<?> artifact = dependency.getArtifact();
        ZipFile zipFile = new ZipFile(artifact.getFullyQualifiedName());
        String destinationFolder = coreRulesPropertiesPath.toAbsolutePath().toString();
        zipFile.extractAll(destinationFolder);
        return Results.success("Sucessfully updated to version " + latestCoordinate.getVersion());
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
