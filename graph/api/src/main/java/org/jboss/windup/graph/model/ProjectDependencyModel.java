package org.jboss.windup.graph.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.List;

/**
 * Project dependency information. Contains all the information that would be required for a Maven dependency, but
 * can also be used for non-maven dependencies. Additional interfaces may extend this to provide further functionality.
 */
@TypeValue(ProjectDependencyModel.TYPE)
public interface ProjectDependencyModel extends WindupVertexFrame {
    String TYPE = "ProjectDependencyModel";
    String PROPERTY_SCOPE = "dependencyScope";
    String PROPERTY_CLASSIFIER = "dependencyClassifier";
    String PROPERTY_TYPE = "dependencyType";
    String FILE_LOCATION_REFERENCE = "fileLocationReference";
    String PROJECT_DEPENDENCY_LOCATION = "dependencyLocation";

    @Property(PROPERTY_SCOPE)
    String getScope();

    @Property(PROPERTY_SCOPE)
    void setScope(String scope);

    @Property(PROPERTY_CLASSIFIER)
    String getClassifier();

    @Property(PROPERTY_CLASSIFIER)
    void setClassifier(String classifier);

    /**
     * Dependency &lt;type&gt; from the Maven POM - jar, war, etc.
     * Might be redundant with getProjectModel().getType()
     * TODO: Remove, not used.
     */
    @Property(PROPERTY_TYPE)
    String getType();

    /**
     * Dependency &lt;type&gt; from the Maven POM - jar, war, etc.
     * Might be redundant with getProjectModel().getType()
     */
    @Property(PROPERTY_TYPE)
    void setType(String type);

    /**
     * A reference to the project represented by this dependency (whether that be a project representing a binary jar,
     * or a project representing a different source module within the current application).
     */
    @Adjacency(label = TYPE + ".representedProject", direction = Direction.OUT)
    void setProject(ProjectModel projectModel);

    @Adjacency(label = TYPE + ".representedProject", direction = Direction.OUT)
    ProjectModel getProjectModel();

    /**
     * Gets the original{@link FileLocationModel} associated with this {@link ProjectDependencyModel}
     */
    @Adjacency(label = FILE_LOCATION_REFERENCE, direction = Direction.OUT)
    List<FileLocationModel> getFileLocationReference();

    /**
     * Sets the original {@link FileLocationModel} associated with this {@link ProjectDependencyModel}
     */
    @Adjacency(label = FILE_LOCATION_REFERENCE, direction = Direction.OUT)
    void setFileLocationReference(List<FileLocationModel> m);

    @Property(PROJECT_DEPENDENCY_LOCATION)
    DependencyLocation getDependencyLocation();

    @Property(PROJECT_DEPENDENCY_LOCATION)
    void setDependencyLocation(DependencyLocation dependencyLocation);
}
