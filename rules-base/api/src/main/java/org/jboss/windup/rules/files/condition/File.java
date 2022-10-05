package org.jboss.windup.rules.files.condition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
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
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterizedPatternResult;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.ocpsoft.rewrite.util.Maps;

/**
 * Matches on file based upon parameterization. This condition is similar to {@link FileContent} condition, however is concerned only
 * with the file metadata and not the content inside.
 * <p/>
 * Example:
 * <p/>
 * <pre>
 *   {@link File}.inFilesNamed("{filename}")
 * </pre>
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class File extends ParameterizedGraphCondition {
    private static final Logger LOG = Logging.get(File.class);

    private RegexParameterizedPatternParser filenamePattern;

    public File() {
    }

    public static FileFrom from(String from) {
        FileFrom f = new FileFrom();
        f.setFrom(from);
        return f;
    }

    public static File inFileNamed(String filenamePattern) {
        File f = new File();
        f.filenamePattern = new RegexParameterizedPatternParser(filenamePattern);
        return f;
    }

    /**
     * Optionally specify the variable name to use for the output of this condition
     */
    public ConditionBuilder as(String variable) {
        Assert.notNull(variable, "Variable name must not be null.");
        this.setOutputVariablesName(variable);
        return this;
    }

    @Override
    public Set<String> getRequiredParameterNames() {
        Set<String> result = new HashSet<>();
        if (filenamePattern != null)
            result.addAll(filenamePattern.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store) {
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
            final ParameterizedPatternResult parsedFileNamePattern = filenamePattern.parse(fileModel.getFileName());
            evaluationStrategy.modelMatched();
            if (parsedFileNamePattern == null || parsedFileNamePattern.submit(event, context)) {
                // Use a file location model to make attaching hints possible
                FileLocationModel fileLocationModel = fileLocationService.create();
                fileLocationModel.setFile(fileModel);
                fileLocationModel.setColumnNumber(1);
                fileLocationModel.setLineNumber(1);
                fileLocationModel.setLength(1);
                fileLocationModel.setSourceSnippit("File Match");

                results.add(fileLocationModel);
                evaluationStrategy.modelSubmitted(fileLocationModel);
            } else {
                evaluationStrategy.modelSubmissionRejected();
            }
        }

        setResults(event, getVarname(), results);
        return !results.isEmpty();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append(".from(").append(getInputVariablesName()).append(")");
        builder.append(".inFilesNamed(").append(filenamePattern).append(")");
        builder.append(".as(").append(getVarname()).append(")");
        return builder.toString();
    }

    /**
     * Generating the input vertices is quite complex. Therefore there are multiple methods that handles the input vertices based on the attribute
     * specified in specific order. This method handles the {@link File#from(String)} attribute.
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
     * specified in specific order. This method handles the {@link File#inFileNamed(String)} attribute.
     */
    private void fileNameInput(List<FileModel> vertices, GraphRewrite event, ParameterStore store) {
        if (this.filenamePattern != null) {
            Pattern filenameRegex = filenamePattern.getCompiledPattern(store);
            //in case the filename is the first operation generating result, we can use the graph regex index. That's why we distinguish that in this clause
            if (vertices.isEmpty() && StringUtils.isBlank(getInputVariablesName())) {
                FileService fileModelService = new FileService(event.getGraphContext());
                for (FileModel fileModel : fileModelService.findByFilenameRegex(filenameRegex.pattern())) {
                    vertices.add(fileModel);
                }
            } else {
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

    public void setFilenamePattern(RegexParameterizedPatternParser filenamePattern) {
        this.filenamePattern = filenamePattern;
    }
}
