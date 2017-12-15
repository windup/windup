package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Extends the file model with some convenience accessors for getting to {@link InlineHintModel} and other reporting related data.
 */
@TypeValue(ReportFileModel.TYPE)
public interface ReportFileModel extends FileModel
{
    String TYPE = "ReportFileModel";
    String RELATED_HINTS_QUERY = "it.in(\"" + FileReferenceModel.FILE_MODEL
                + "\").has(\"" + WindupVertexFrame.TYPE_PROP
                + "\", com.thinkaurelius.titan.core.attribute.Text.CONTAINS, \"" + InlineHintModel.TYPE + "\")";
    String RELATED_CLASSIFICATIONS_QUERY = "it.in(\"" + ClassificationModel.FILE_MODEL + "\")";

    /**
     * Get the number of {@link InlineHintModel} instances attached to this {@link ReportFileModel}
     */
    @GremlinGroovy(frame = false, value = RELATED_HINTS_QUERY + ".count()")
    long getInlineHintCount();

    /**
     * Get all {@link InlineHintModel} instances attached to this {@link ReportFileModel}
     */
    @GremlinGroovy(frame = true, value = RELATED_HINTS_QUERY)
    Iterable<InlineHintModel> getInlineHints();

    /**
     * Get all {@link ClassificationModel} instances attached to this {@link ReportFileModel}
     */
    @Adjacency(label = ClassificationModel.FILE_MODEL, direction = Direction.IN)
    Iterable<ClassificationModel> getClassificationModels();

    /**
     * Get the number of {@link ClassificationModel} instances attached to this {@link ReportFileModel}
     */
    @GremlinGroovy(frame = false, value = RELATED_CLASSIFICATIONS_QUERY + ".count()")
    long getClassificationCount();
}
