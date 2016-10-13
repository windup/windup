package org.jboss.windup.rules.files.condition;

import org.jboss.windup.graph.model.resource.FileModel;

/**
 * An interface to reflect an ability of an model to be transformed to {@link FileModel}.
 */
public interface ToFileModelTransformable
{
    /**
     * Transform the given model into multiple {@link FileModel}s.
     * This is useful especially for operation on graph conditions such as filtering.
     * @return {@FileModel} filemodels
     */
    Iterable<FileModel> transformToFileModel();
}
