package org.jboss.windup.reporting.model;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Extends the file model with some convenience accessors for getting to {@link InlineHintModel} and other reporting related data.
 */
@TypeValue(ReportFileModel.TYPE)
public interface ReportFileModel extends FileModel {
    String TYPE = "ReportFileModel";

    /**
     * Get the number of {@link InlineHintModel} instances attached to this {@link ReportFileModel}
     */
    default long getInlineHintCount() {
        return getInlineHints().size();
    }

    /**
     * Get all {@link InlineHintModel} instances attached to this {@link ReportFileModel}
     */
    default List<InlineHintModel> getInlineHints() {
        List<Vertex> vertices = new GraphTraversalSource(getWrappedGraph().getBaseGraph()).V(getElement())
                .in(FileReferenceModel.FILE_MODEL)
                .has(WindupVertexFrame.TYPE_PROP, InlineHintModel.TYPE)
                .toList();
        return vertices.stream().map(v -> getGraph().frameElement(v, InlineHintModel.class))
                .collect(Collectors.toList());
    }

    /**
     * Get all {@link ClassificationModel} instances attached to this {@link ReportFileModel}
     */
    @Adjacency(label = ClassificationModel.FILE_MODEL, direction = Direction.IN)
    default List<ClassificationModel> getClassificationModels() {
        List<Vertex> vertices = new GraphTraversalSource(getWrappedGraph().getBaseGraph()).V(getElement())
                .in(ClassificationModel.FILE_MODEL)
                .toList();
        return vertices.stream().map(v -> getGraph().frameElement(v, ClassificationModel.class))
                .collect(Collectors.toList());
    }

    /**
     * Get the number of {@link ClassificationModel} instances attached to this {@link ReportFileModel}
     */
    default long getClassificationCount() {
        return getClassificationModels().size();
    }
}
