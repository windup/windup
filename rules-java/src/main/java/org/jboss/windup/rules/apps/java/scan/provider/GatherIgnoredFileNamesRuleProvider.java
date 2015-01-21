package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.IteratingRuleProvider;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * Read and add all the ignore regexes that are present in the windup runtime.
 * Files matched by the regex will not be scanned by Windup.
 * Adds: IgnoredFileRegexModel's to WindupJavaConfigurationModel.
 *
 * @author mbriskar
 */
public class GatherIgnoredFileNamesRuleProvider extends IteratingRuleProvider<WindupConfigurationModel>
{

    private final String IGNORE_FILE_EXTENSION = "windup-ignore.txt";
    private static final Logger log = Logger.getLogger(GatherIgnoredFileNamesRuleProvider.class.getName());

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
    {
        return asClassList(UnzipArchivesToOutputRuleProvider.class);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel windupConfigM)
    {
        WindupJavaConfigurationModel javaCfg = WindupJavaConfigurationService.getJavaConfigurationModel(event.getGraphContext());
        final List<Path> filesUrl = new ArrayList<>();
        for (FileModel ignoredRegexesFileModel : windupConfigM.getUserIgnorePaths())
        {

            // Files pointed to by user are read regardless its name.
            if (!ignoredRegexesFileModel.isDirectory()){
                filesUrl.add(Paths.get(ignoredRegexesFileModel.getFilePath()));
                continue;
            }

            // Directories are searched for files ending with IGNORE_FILE_EXTENSION ("windup-ignore.txt").
            try
            {
                Files.walkFileTree(Paths.get(ignoredRegexesFileModel.getFilePath()), new SimpleFileVisitor<Path>()
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
            catch (IOException ex)
            {
                log.log(Level.WARNING, "Failed reading the ignored file regexes in "
                        + ignoredRegexesFileModel.getFilePath() + ex.getMessage(), ex);
            }
        }

        // Load regexes from the files found.
        for (Path filePath : filesUrl)
        {
            readAndAddFileRegexes(filePath, javaCfg, event.getGraphContext());
        }
    }


    private void readAndAddFileRegexes(Path filePath, WindupJavaConfigurationModel javaCfg, GraphContext gCtx)
    {
        File file = filePath.toFile();
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file));)
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                GraphService<IgnoredFileRegexModel> graphService = new GraphService(gCtx, IgnoredFileRegexModel.class);
                IgnoredFileRegexModel ignored = graphService.create();
                ignored.setRegex(line);
                javaCfg.addIgnoredFileRegex(ignored);
            }
        }
        catch (IOException e)
        {
        }
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(WindupConfigurationModel.class);
    }

    @Override
    public String toStringPerform()
    {
        return "Gather all the information about ignored files.";
    }
}
