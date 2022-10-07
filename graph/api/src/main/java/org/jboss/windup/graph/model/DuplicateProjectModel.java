package org.jboss.windup.graph.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;

/**
 * Represents a {@link ProjectModel} that is actually an exact duplicate of another project.
 * <p>
 * The duplicate will generally contain no files and the canonical project ({@see DuplicateProjectModel#getCanonicalProject})
 * should be used for finding the included files.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(DuplicateProjectModel.TYPE)
public interface DuplicateProjectModel extends ProjectModel {
    String TYPE = "DuplicateProjectModel";
    String CANONICAL_PROJECT = TYPE + "-canonicalProject";

    /**
     * Contains a reference to the canonical (non-duplicated) project.
     */
    @Adjacency(label = CANONICAL_PROJECT, direction = Direction.OUT)
    ProjectModel getCanonicalProject();

    /**
     * Contains a reference to the canonical (non-duplicated) project.
     */
    @Adjacency(label = CANONICAL_PROJECT, direction = Direction.OUT)
    DuplicateProjectModel setCanonicalProject(ProjectModel original);
}
