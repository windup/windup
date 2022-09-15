package org.jboss.windup.reporting.config;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parameters.ParameterizedIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternBuilder;

import java.util.*;
import java.util.logging.Logger;

/**
 * Classifies a {@link FileModel} {@link Iteration} payload.
 */
public class TechnologyTag extends ParameterizedIterationOperation<FileModel> {
    private static final Logger LOG = Logging.get(TechnologyTag.class);

    private final RegexParameterizedPatternBuilder nameBuilder;
    private TechnologyTagLevel technologyTagLevel;

    private TechnologyTag(String tagName) {
        this.nameBuilder = new RegexParameterizedPatternBuilder(tagName);
        this.technologyTagLevel = TechnologyTagLevel.INFORMATIONAL;
    }

    public static TechnologyTag withName(String tagName) {
        return new TechnologyTag(tagName);
    }

    public TechnologyTag withTechnologyTagLevel(TechnologyTagLevel technologyTagLevel) {
        this.technologyTagLevel = technologyTagLevel;
        return this;
    }

    @Override
    public FileModel resolvePayload(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload) {
        checkVariableName(event, context);
        if (payload instanceof FileReferenceModel) {
            return ((FileReferenceModel) payload).getFile();
        }
        if (payload instanceof FileModel) {
            return (FileModel) payload;
        }
        return null;
    }

    @Override
    public void performParameterized(GraphRewrite event, EvaluationContext context, FileModel payload) {
        ExecutionStatistics.get().begin("TechnologyTag.performParameterized");
        try {
            GraphContext graphContext = event.getGraphContext();
            TechnologyTagService technologyTagService = new TechnologyTagService(graphContext);
            technologyTagService.addTagToFileModel(payload, this.nameBuilder.build(event, context), this.technologyTagLevel);
            LOG.info("TechnologyTag added to " + payload.getPrettyPathWithinProject() + " [" + this + "] ");
        } finally {
            ExecutionStatistics.get().end("TechnologyTag.performParameterized");
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("TechnologyTag.withName(").append(this.nameBuilder.toString()).append(")");
        result.append(".withTechnologyTagLevel(").append(this.technologyTagLevel).append(")");
        return result.toString();
    }

    @Override
    public Set<String> getRequiredParameterNames() {
        return new HashSet<>(this.nameBuilder.getRequiredParameterNames());
    }

    @Override
    public void setParameterStore(ParameterStore store) {
        this.nameBuilder.setParameterStore(store);
    }

}
