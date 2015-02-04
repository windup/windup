package org.jboss.windup.rules.files.condition;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.EvaluationStrategy;
import org.jboss.windup.config.condition.NoopEvaluationStrategy;
import org.jboss.windup.config.parameters.FrameContext;
import org.jboss.windup.config.parameters.FrameCreationContext;
import org.jboss.windup.config.parameters.ParameterizedGraphCondition;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.files.condition.regex.StreamRegexMatchListener;
import org.jboss.windup.rules.files.condition.regex.StreamRegexMatchedEvent;
import org.jboss.windup.rules.files.condition.regex.StreamRegexMatcher;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterizedPatternResult;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.ocpsoft.rewrite.util.Maps;

import com.googlecode.streamflyer.core.Modifier;
import com.googlecode.streamflyer.core.ModifyingReader;

/**
 * Matches on file contents based upon parameterization.
 * 
 * Example:
 * 
 * <pre>
 *   {@link FileContent}.matches("Some example {text}").inFilesNamed("{filename}")
 * </pre>
 * 
 * @author jsightler
 *
 */
public class FileContent extends ParameterizedGraphCondition implements FileContentMatches
{
    private static Logger LOG = Logging.get(FileContent.class);

    private RegexParameterizedPatternParser contentPattern;
    private RegexParameterizedPatternParser filenamePattern;

    public FileContent(String contentPattern)
    {
        this.contentPattern = new RegexParameterizedPatternParser(contentPattern);
    }

    /**
     * Match file contents against the provided parameterized string.
     */
    public static FileContentMatches matches(String contentPattern)
    {
        return new FileContent(contentPattern);
    }

    /**
     * Match filenames against the provided parameterized string.
     */
    public FileContent inFilesNamed(String filenamePattern)
    {
        this.filenamePattern = new RegexParameterizedPatternParser(filenamePattern);
        return this;
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        Set<String> result = new HashSet<>(contentPattern.getRequiredParameterNames());
        if (filenamePattern != null)
            result.addAll(filenamePattern.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        contentPattern.setParameterStore(store);
        if (filenamePattern != null)
            filenamePattern.setParameterStore(store);
    }

    @Override
    protected String getVarname()
    {
        return getOutputVariablesName();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean evaluateAndPopulateValueStores(GraphRewrite event, EvaluationContext context, final FrameCreationContext frameCreationContext)
    {
        return evaluate(event, context, new EvaluationStrategy()
        {
            private LinkedHashMap<String, List<WindupVertexFrame>> variables;

            @Override
            @SuppressWarnings("rawtypes")
            public void modelMatched()
            {
                this.variables = new LinkedHashMap<String, List<WindupVertexFrame>>();
                frameCreationContext.beginNew((Map) variables);
            }

            @Override
            public void modelSubmitted(WindupVertexFrame model)
            {
                Maps.addListValue(this.variables, getVarname(), model);
            }

            @Override
            public void modelSubmissionRejected()
            {
                frameCreationContext.rollback();
            }
        });
    }

    @Override
    protected boolean evaluateWithValueStore(GraphRewrite event, EvaluationContext context, final FrameContext frameContext)
    {
        boolean result = evaluate(event, context, new NoopEvaluationStrategy());

        if (result == false)
            frameContext.reject();

        return result;
    }

    private boolean evaluate(final GraphRewrite event, final EvaluationContext context,
                final EvaluationStrategy evaluationStrategy)
    {
        final ParameterStore store = DefaultParameterStore.getInstance(context);

        final GraphService<FileLocationModel> fileLocationService = new GraphService<>(event.getGraphContext(), FileLocationModel.class);
        FileService fileModelService = new FileService(event.getGraphContext());
        final Iterable<FileModel> fileModels;
        if (filenamePattern != null)
        {
            Pattern filenameRegex = filenamePattern.getCompiledPattern(store);
            fileModels = fileModelService.findAllByPropertyMatchingRegex(FileModel.FILE_NAME, filenameRegex.pattern());
        }
        else
        {
            fileModels = fileModelService.findAll();
        }

        final List<FileLocationModel> results = new ArrayList<>();
        for (final FileModel fileModel : fileModels)
        {
            if (fileModel.isDirectory())
                continue;

            final ParameterizedPatternResult filenamePatternResult = filenamePattern.parse(fileModel.getFileName());
            if (filenamePatternResult.matches())
            {
                try
                {
                    Reader reader = new FileReader(fileModel.asFile());

                    Pattern fileContentsRegex = contentPattern.getCompiledPattern(store);
                    StreamRegexMatchListener matchListener = new StreamRegexMatchListener()
                    {

                        @Override
                        public void regexMatched(StreamRegexMatchedEvent matchEvent)
                        {
                            String matchedStr = matchEvent.getMatch();
                            ParameterizedPatternResult contentPatternResult = contentPattern.parse(matchedStr);
                            if (contentPatternResult.matches())
                            {
                                evaluationStrategy.modelMatched();
                                if (filenamePatternResult.submit(event, context)
                                            && contentPatternResult.submit(event, context))
                                {
                                    FileLocationModel fileLocationModel = fileLocationService.create();
                                    fileLocationModel.setFile(fileModel);
                                    fileLocationModel.setColumnNumber((int) matchEvent.getColumnNumber());
                                    // increment by one, as the source is 0-based, but the model is 1-based
                                    int lineNumber = (int) (matchEvent.getLineNumber() + 1);
                                    fileLocationModel.setLineNumber(lineNumber);
                                    fileLocationModel.setLength(matchedStr.length());
                                    fileLocationModel.setSourceSnippit(matchedStr);
                                    results.add(fileLocationModel);
                                    evaluationStrategy.modelSubmitted(fileLocationModel);
                                }
                                else
                                {
                                    evaluationStrategy.modelSubmissionRejected();
                                }
                            }
                        }
                    };

                    Modifier regexModifier = new StreamRegexMatcher(fileContentsRegex.pattern(), matchListener);
                    try (ModifyingReader modifyingReader = new ModifyingReader(reader, regexModifier))
                    {

                        char[] buffer = new char[32768];
                        while (modifyingReader.read(buffer) != -1)
                            ; // consume the stream
                    }
                }
                catch (Exception e)
                {
                    LOG.log(Level.WARNING, "Error loading and matching contents for file: " + fileModel.getFilePath() + " due to: " + e.getMessage(),
                                e);
                }
            }
        }

        Variables.instance(event).setVariable(getVarname(), results);
        return !results.isEmpty();
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append(".matches(" + contentPattern + ")");
        builder.append(".inFilesNamed(" + filenamePattern + ")");
        builder.append(".as(" + getVarname() + ")");
        return builder.toString();
    }
}
