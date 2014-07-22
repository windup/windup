package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Extends the file model with some convenience accessors for getting to BlackLists and other reporting related data.
 */
@TypeValue("ReportFileModel")
public interface ReportFileModel extends FileModel
{
    @Adjacency(label = BlackListModel.FILE_MODEL, direction = Direction.IN)
    public Iterable<BlackListModel> getBlackListModels();

    @Adjacency(label = ClassificationModel.FILE_MODEL, direction = Direction.IN)
    public Iterable<ClassificationModel> getClassificationModels();
}
