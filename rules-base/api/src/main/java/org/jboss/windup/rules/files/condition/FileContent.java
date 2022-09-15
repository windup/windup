package org.jboss.windup.rules.files.condition;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.EvaluationStrategy;
import org.jboss.windup.config.condition.NoopEvaluationStrategy;
import org.jboss.windup.config.parameters.FrameContext;
import org.jboss.windup.config.parameters.FrameCreationContext;
import org.jboss.windup.config.parameters.ParameterizedGraphCondition;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.files.condition.regex.StreamRegexMatchListener;
import org.jboss.windup.rules.files.condition.regex.StreamRegexMatchedEvent;
import org.jboss.windup.rules.files.condition.regex.StreamRegexMatcher;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterizedPatternResult;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.ocpsoft.rewrite.util.Maps;

import com.github.rwitzel.streamflyer.core.Modifier;
import com.github.rwitzel.streamflyer.core.ModifyingReader;

/**
 * Matches on file contents based upon parameterization.
 * <p/>
 * Example:
 * <p/>
 * <pre>
 *   {@link FileContent}.matches("Some example {text}").inFilesNamed("{filename}")
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FileContent extends ParameterizedGraphCondition implements FileContentMatches, FileContentFileName {
    private static Logger LOG = Logging.get(FileContent.class);

    private RegexParameterizedPatternParser contentPattern;
    private RegexParameterizedPatternParser filenamePattern;

    public FileContent() {
    }

    public FileContent(String contentPattern) {
        this.contentPattern = new RegexParameterizedPatternParser(contentPattern);
    }

    public static FileContentFrom from(String from) {
        FileContentFromImpl f = new FileContentFromImpl(from);
        return f;
    }

    /**
     * Match file contents against the provided parameterized string.
     */
    public static FileContentMatches matches(String contentPattern) {
        return new FileContent(contentPattern);
    }

    /**
     * Optionally specify the variable name to use for the output of this condition
     */
    public ConditionBuilder as(String variable) {
        Assert.notNull(variable, "Variable name must not be null.");
        this.setOutputVariablesName(variable);
        return this;
    }

    /**
     * Match filenames against the provided parameterized string.
     */
    public FileContentFileName inFileNamed(String filenamePattern) {
        if (filenamePattern != null && !filenamePattern.isEmpty()) {
            this.filenamePattern = new RegexParameterizedPatternParser(filenamePattern);
        }
        return this;
    }

    @Override
    public Set<String> getRequiredParameterNames() {
        Set<String> result = new HashSet<>();
        if (contentPattern != null) {
            result.addAll(contentPattern.getRequiredParameterNames());
        }
        if (filenamePattern != null)
            result.addAll(filenamePattern.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store) {
        if (contentPattern != null)
            contentPattern.setParameterStore(store);
        if (filenamePattern != null)
            filenamePattern.setParameterStore(store);
    }

    @Override
    protected String getVarname() {
        return getOutputVariablesName();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean evaluateAndPopulateValueStores(GraphRewrite event, EvaluationContext context, final FrameCreationContext frameCreationContext) {
        return evaluate(event, context, new EvaluationStrategy() {
            private LinkedHashMap<String, List<WindupVertexFrame>> variables;

            @Override
            @SuppressWarnings("rawtypes")
            public void modelMatched() {
                this.variables = new LinkedHashMap<>();
                frameCreationContext.beginNew((Map) variables);
            }

            @Override
            public void modelSubmitted(WindupVertexFrame model) {
                Maps.addListValue(this.variables, getVarname(), model);
            }

            @Override
            public void modelSubmissionRejected() {
                frameCreationContext.rollback();
            }
        });
    }

    @Override
    protected boolean evaluateWithValueStore(GraphRewrite event, EvaluationContext context, final FrameContext frameContext) {
        boolean result = evaluate(event, context, new NoopEvaluationStrategy());

        if (result == false)
            frameContext.reject();

        return result;
    }

    private boolean evaluate(final GraphRewrite event, final EvaluationContext context,
                             final EvaluationStrategy evaluationStrategy) {
        final ParameterStore store = DefaultParameterStore.getInstance(context);
        final GraphService<FileLocationModel> fileLocationService = new GraphService<>(event.getGraphContext(), FileLocationModel.class);

        // initialize the input
        List<FileModel> fileModels = new ArrayList<>();
        fromInput(fileModels, event);
        fileNameInput(fileModels, event, store);
        allInput(fileModels, event, store);

        final List<FileLocationModel> results = new ArrayList<>();
        for (final FileModel fileModel : fileModels) {
            if (fileModel.isDirectory())
                continue;
            ParameterizedPatternResult parsedFileNamePattern = null;
            if (filenamePattern != null) {
                parsedFileNamePattern = filenamePattern.parse(fileModel.getFileName());
                if (!parsedFileNamePattern.matches()) {
                    continue;
                }
            }
            //because it is used in the internal method definition
            final ParameterizedPatternResult parsedFileNamePattern2 = parsedFileNamePattern;
            try {
                Reader reader = new FileReader(fileModel.asFile());

                Pattern fileContentsRegex = contentPattern.getCompiledPattern(store);
                StreamRegexMatchListener matchListener = new StreamRegexMatchListener() {

                    @Override
                    public void regexMatched(StreamRegexMatchedEvent matchEvent) {
                        String matchedStr = matchEvent.getMatch();
                        boolean passed = true;
                        ParameterizedPatternResult contentPatternResult = null;
                        if (contentPattern != null) {
                            contentPatternResult = contentPattern.parse(matchedStr);
                            passed = passed && contentPatternResult.matches();
                        }
                        if (passed) {
                            evaluationStrategy.modelMatched();
                            if ((parsedFileNamePattern2 == null || parsedFileNamePattern2.submit(event, context))
                                    && (contentPattern == null || contentPatternResult.submit(event, context))) {
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
                            } else {
                                evaluationStrategy.modelSubmissionRejected();
                            }
                        }
                    }
                };

                Modifier regexModifier = StreamRegexMatcher.create(fileContentsRegex.pattern(), matchListener);
                try (ModifyingReader modifyingReader = new ModifyingReader(reader, regexModifier)) {

                    char[] buffer = new char[32768];
                    while (modifyingReader.read(buffer) != -1)
                        ; // consume the stream
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error loading and matching contents for file: " + fileModel.getFilePath() + " due to: " + e.getMessage(),
                        e);
            }
        }

        setResults(event, getVarname(), results);
        return !results.isEmpty();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append(".from(").append(getInputVariablesName()).append(")");
        builder.append(".matches(").append(contentPattern).append(")");
        builder.append(".inFilesNamed(").append(filenamePattern).append(")");
        builder.append(".as(").append(getVarname()).append(")");
        return builder.toString();
    }

    /**
     * Generating the input vertices is quite complex. Therefore there are multiple methods that handles the input vertices based on the attribute
     * specified in specific order. This method handles the {@link FileContent#from(String)} attribute.
     */
    private void fromInput(List<FileModel> vertices, GraphRewrite event) {
        if (vertices.isEmpty() && StringUtils.isNotBlank(getInputVariablesName())) {
            for (WindupVertexFrame windupVertexFrame : Variables.instance(event).findVariable(getInputVariablesName())) {
                if (windupVertexFrame instanceof FileModel)
                    vertices.add((FileModel) windupVertexFrame);
                if (windupVertexFrame instanceof FileReferenceModel)
                    vertices.add(((FileReferenceModel) windupVertexFrame).getFile());
            }
        }
    }

    /**
     * Generating the input vertices is quite complex. Therefore there are multiple methods that handles the input vertices based on the attribute
     * specified in specific order. This method handles the {@link FileContent#inFileNamed(String)} attribute.
     */
    private void fileNameInput(List<FileModel> vertices, GraphRewrite event, ParameterStore store) {
        if (this.filenamePattern != null) {
            Pattern filenameRegex = filenamePattern.getCompiledPattern(store);
            //in case the filename is the first operation generating result, we can use the graph regex index. That's why we distinguish that in this clause
            if (vertices.isEmpty() && StringUtils.isBlank(getInputVariablesName())) {
                FileService fileService = new FileService(event.getGraphContext());
                for (FileModel fileModel : fileService.findByFilenameRegex(filenameRegex.pattern())) {
                    vertices.add(fileModel);
                }
            } else {
                ListIterator<FileModel> fileModelIterator = vertices.listIterator();
                vertices.removeIf(fileModel -> !filenameRegex.matcher(fileModel.getFileName()).matches());
            }
        }
    }

    /**
     * Generating the input vertices is quite complex. Therefore there are multiple methods that handles the input vertices
     * based on the attribute specified in specific order. This method generates all the vertices if there is no other way how to handle
     * the input.
     */
    private void allInput(List<FileModel> vertices, GraphRewrite event, ParameterStore store) {
        if (StringUtils.isBlank(getInputVariablesName()) && this.filenamePattern == null) {
            FileService fileModelService = new FileService(event.getGraphContext());
            for (FileModel fileModel : fileModelService.findAll()) {
                vertices.add(fileModel);
            }
        }
    }
}
