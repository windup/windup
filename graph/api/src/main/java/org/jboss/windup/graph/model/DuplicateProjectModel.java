package org.jboss.windup.graph.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(DuplicateProjectModel.TYPE)
public interface DuplicateProjectModel extends ProjectModel
{
    String TYPE = "DuplicateProject";
    String ORIGINAL_PROJECT = "originalProject";

    @Adjacency(label = ORIGINAL_PROJECT, direction = Direction.OUT)
    ProjectModel getOriginalProject();

    @Adjacency(label = ORIGINAL_PROJECT, direction = Direction.OUT)
    DuplicateProjectModel setOriginalProject(ProjectModel original);
}
