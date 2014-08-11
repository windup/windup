package org.jboss.windup.reporting.rules;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CssJsResourceRenderingRuleProvider extends WindupRuleProvider
{

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_RENDERING;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder configSearch = Query.find(WindupConfigurationModel.class);

        Configuration configuration = ConfigurationBuilder.begin()
        .addRule()
        .when(configSearch)
        .perform(
            Iteration.over()
            .perform(
                new AbstractIterationOperation<WindupConfigurationModel>(
                            WindupConfigurationModel.class)
                {
                    public void perform(GraphRewrite event,
                        EvaluationContext context, WindupConfigurationModel payload)
                    {
                        String outputPath = payload.getOutputPath().getFilePath();
                        copyCssResourcesToOutput(outputPath);
                    }
                }
            ).endIteration()
        );
        return configuration;
    }
    // @formatter:on

    private void copyCssResourcesToOutput(String outputDir)
    {
        Path outputPath = Paths.get(outputDir, "resources");
        try
        {
            String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            if (path.endsWith(".jar") || path.endsWith(".jar!/"))
            {
                path = path.replace("file:", "jar:file:");
            }

            File fpath = new File(path);
            if (fpath.isDirectory())
            {
                Path p = Paths.get(fpath.getAbsolutePath(), "reports/resources");
                recursePath(p, outputPath);
            }
            else
            {
                FileSystem fs = FileSystems.newFileSystem(new URI(path), new HashMap<String, String>());
                Path p = fs.getPath("reports/resources");
                recursePath(p, outputPath);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception reading resource.", e);
        }
    }

    public void recursePath(final Path path, final Path resultPath) throws IOException
    {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                String relativePath = StringUtils.substringAfter(file.toString(), path.toString());
                relativePath = StringUtils.removeStart(relativePath, File.separator);
                Path resultFile = resultPath.resolve(relativePath);

                FileUtils.forceMkdir(resultFile.getParent().toFile());
                Files.copy(file, resultFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
