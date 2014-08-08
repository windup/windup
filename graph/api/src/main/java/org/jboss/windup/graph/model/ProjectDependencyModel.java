package org.jboss.windup.graph.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Project dependency information. This has all of the information that would be required for a Maven dependency, but
 * can also be used for non-maven dependencies. Additional interfaces may extend this to provide further functionality.
 */
@TypeValue(ProjectDependencyModel.TYPE)
public interface ProjectDependencyModel extends WindupVertexFrame
{
    public static final String TYPE = "ProjectDependency";
    public static final String PROPERTY_SCOPE = "dependencyScope";
    public static final String PROPERTY_CLASSIFIER = "dependencyClassifier";
    public static final String PROPERTY_TYPE = "dependencyType";

    @Property(PROPERTY_SCOPE)
    void setScope(String scope);

    @Property(PROPERTY_SCOPE)
    String getScope();

    @Property(PROPERTY_CLASSIFIER)
    void setClassifier(String classifier);

    @Property(PROPERTY_CLASSIFIER)
    String getClassifier();

    @Property(PROPERTY_TYPE)
    void setType(String type);

    @Property(PROPERTY_TYPE)
    void getType();

    /**
     * A reference to the project represented by this dependency (whether that be a project representing a binary jar,
     * or a project representing a different source module within the current application).
     */
    @Adjacency(label = "project", direction = Direction.OUT)
    void setProject(ProjectModel projectModel);

    @Adjacency(label = "project", direction = Direction.OUT)
    ProjectModel getProjectModel();
}
