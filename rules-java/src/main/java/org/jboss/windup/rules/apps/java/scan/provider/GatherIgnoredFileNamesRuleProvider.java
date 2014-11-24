package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.IteratingRuleProvider;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * Read the regular expressions of file paths to ignore, from user-provided files {@link UserIgnorePathOption}
 * and directories and add them to graph {@link IgnoredFileRegexModel};
 * When a file matches the regex, it will not be scanned by windup.
 * Input paths that are files are read directly.
 * Input paths that are directories are searched recursively for all *windup-ignore.txt, which are then read.
 * Lines starting with # are treated as comments.
 *
 * @author mbriskar
 */
public class GatherIgnoredFileNamesRuleProvider extends IteratingRuleProvider<WindupConfigurationModel>
{
    private static final Logger log = Logger.getLogger(GatherIgnoredFileNamesRuleProvider.class.getName());

    private final String IGNORE_FILE_EXTENSION = "windup-ignore.txt";
    private static final int MAX_REGEX_FILE_SIZE_KB = 100;


    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        super.enhanceMetadata(context);
        context.put(RuleMetadata.CATEGORY, "Core");
    }

    public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
    {
        return asClassList(UnzipArchivesToOutputRuleProvider.class);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload)
    {
        WindupJavaConfigurationModel javaCfg =
                WindupJavaConfigurationService.getJavaConfigurationModel(event.getGraphContext());

        final List<Path> filesUrl = new ArrayList<>();

        // For each user-provided ignore path...
        for (FileModel ignoredRegexesFileModel : payload.getUserIgnorePaths())
        {
            final Path ignoredPath = Paths.get(ignoredRegexesFileModel.getFilePath());

            // Add non-directories.
            if ( ! ignoredRegexesFileModel.isDirectory())
            {
                filesUrl.add(ignoredPath);
                continue;
            }

            try
            {
                // Search for the regex files.
                Files.walkFileTree(ignoredPath, new SimpleFileVisitor<Path>()
                {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                    {
                        if (file.getFileName().toString().toLowerCase().endsWith(IGNORE_FILE_EXTENSION))
                            filesUrl.add(file);
                        return FileVisitResult.CONTINUE;
                    }
                });

            }
            catch (IOException e1)
            {
                log.warning("IOException thrown when trying to access the ignored file regexes in " + ignoredRegexesFileModel.getFilePath());
            }
        }

        for (Path filePath : filesUrl)
        {
            readAndAddFileRegexes(filePath, javaCfg, event.getGraphContext());
        }
    }

    private void readAndAddFileRegexes(Path filePath, WindupJavaConfigurationModel javaCfg, GraphContext context)
    {
        File file = filePath.toFile();
        if (!file.exists())
            return;
        if (file.length() > MAX_REGEX_FILE_SIZE_KB * 1024){
            log.warning("File with ignored paths regex exceeds maximum of " + MAX_REGEX_FILE_SIZE_KB + " kB: " + filePath.toString());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file));)
        {
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                // Allow # comments
                if (line.trim().startsWith("#"))
                    continue;

                GraphService<IgnoredFileRegexModel> graphService = new GraphService<IgnoredFileRegexModel>(
                            context, IgnoredFileRegexModel.class);
                IgnoredFileRegexModel ignored = graphService.create();
                ignored.setRegex(line);
                javaCfg.addIgnoredFileRegex(ignored);
            }
        }
        catch (FileNotFoundException e)
        {
        }
        catch (IOException e)
        {
        }
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.find(WindupConfigurationModel.class);
    }

    @Override
    public String toStringPerform()
    {
        return "Gather all the information about ignored files.";
    }
}
