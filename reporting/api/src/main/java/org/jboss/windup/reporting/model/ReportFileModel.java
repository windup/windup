package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.files.model.FileReferenceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Extends the file model with some convenience accessors for getting to {@link InlineHintModel} and other reporting
 * related data.
 */
@TypeValue(ReportFileModel.TYPE)
public interface ReportFileModel extends FileModel
{
    public static final String TYPE = "ReportFileModel";
    public static final String RELATED_HINTS_QUERY = "it.in(\"" + FileReferenceModel.FILE_MODEL
                + "\").has(\"" + WindupVertexFrame.TYPE_PROP
                + "\", com.thinkaurelius.titan.core.attribute.Text.CONTAINS, \"" + InlineHintModel.TYPE + "\")";

    /**
     * Get the number of {@link InlineHintModel} instances attached to this {@link ReportFileModel}
     */
    @GremlinGroovy(frame = false, value = RELATED_HINTS_QUERY + ".count()")
    public long getInlineHintCount();

    /**
     * Get all {@link InlineHintModel} instances attached to this {@link ReportFileModel}
     */
    @GremlinGroovy(frame = true, value = RELATED_HINTS_QUERY)
    public Iterable<InlineHintModel> getInlineHints();

    /**
     * Get all {@link ClassificationModel} instances attached to this {@link ReportFileModel}
     */
    @Adjacency(label = ClassificationModel.FILE_MODEL, direction = Direction.IN)
    public Iterable<ClassificationModel> getClassificationModels();
}
