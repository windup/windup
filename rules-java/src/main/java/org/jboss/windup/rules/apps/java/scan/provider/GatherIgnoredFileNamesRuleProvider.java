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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.IteratingRuleProvider;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.phase.Initialization;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.config.CopyJavaConfigToGraphRuleProvider;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Read and add all the ignore regexes (when a file matches the regex, it will not be scanned by windup) that are present in the windup runtime.
 * 
 * @author mbriskar
 *
 */
public class GatherIgnoredFileNamesRuleProvider extends IteratingRuleProvider<WindupConfigurationModel>
{

    private final String IGNORE_FILE_EXTENSION = "windup-ignore.txt";
    private static final Logger log = Logger.getLogger(GatherIgnoredFileNamesRuleProvider.class.getName());

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return Initialization.class;
    }

    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(CopyJavaConfigToGraphRuleProvider.class);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload)
    {
        WindupJavaConfigurationModel javaCfg = WindupJavaConfigurationService.getJavaConfigurationModel(event
                    .getGraphContext());
        final List<Path> filesUrl = new ArrayList<>();
        for (FileModel ignoredRegexesFileModel : payload.getUserIgnorePaths())
        {

            if (ignoredRegexesFileModel.isDirectory())
            {
                try
                {
                    Files.walkFileTree(Paths.get(ignoredRegexesFileModel.getFilePath()), new SimpleFileVisitor<Path>()
                    {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                        {
                            if (file.getFileName().toString().toLowerCase().endsWith(IGNORE_FILE_EXTENSION))
                            {
                                filesUrl.add(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });

                }
                catch (IOException e1)
                {
                    log.warning("IOException thrown when trying to access the ignored file regexes in " + ignoredRegexesFileModel.getFilePath());
                }
            }
            else
            {
                filesUrl.add(Paths.get(ignoredRegexesFileModel.getFilePath()));
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
        if (file.exists())
        {
            try (BufferedReader reader = new BufferedReader(new FileReader(file));)
            {
                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    GraphService<IgnoredFileRegexModel> graphService = new GraphService<IgnoredFileRegexModel>(
                                context, IgnoredFileRegexModel.class);
                    IgnoredFileRegexModel ignored = graphService.create();
                    ignored.setRegex(line);
                    javaCfg.addIgnoredFileRegex(ignored);
                    try
                    {
                        Pattern.compile(line);
                    }
                    catch (PatternSyntaxException exception)
                    {
                        ignored.setCompilationError(exception.getMessage());
                    }
                }
            }
            catch (FileNotFoundException e)
            {
            }
            catch (IOException e)
            {
            }
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
