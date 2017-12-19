package org.jboss.windup.graph.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.syncleus.ferma.annotations.Adjacency;
import com.syncleus.ferma.annotations.Property;

/**
 * Project dependency information. This has all of the information that would be required for a Maven dependency, but
 * can also be used for non-maven dependencies. Additional interfaces may extend this to provide further functionality.
 */
@TypeValue(ProjectDependencyModel.TYPE)
public interface ProjectDependencyModel extends WindupVertexFrame
{
    String TYPE = "ProjectDependencyModel";
    String PROPERTY_SCOPE = "dependencyScope";
    String PROPERTY_CLASSIFIER = "dependencyClassifier";
    String PROPERTY_TYPE = "dependencyType";
    String FILE_LOCATION_REFERENCE = "fileLocationReference";

    @Property(PROPERTY_SCOPE)
    void setScope(String scope);

    @Property(PROPERTY_SCOPE)
    String getScope();

    @Property(PROPERTY_CLASSIFIER)
    void setClassifier(String classifier);

    @Property(PROPERTY_CLASSIFIER)
    String getClassifier();

    /**
     * Dependency &lt;type&gt; from the Maven POM - jar, war, etc.
     * Might be redundant with getProjectModel().getType()
     */
    @Property(PROPERTY_TYPE)
    void setType(String type);

    /**
     * Dependency &lt;type&gt; from the Maven POM - jar, war, etc.
     * Might be redundant with getProjectModel().getType()
     * TODO: Remove, not used.
     */
    @Property(PROPERTY_TYPE)
    String getType();

    /**
     * A reference to the project represented by this dependency (whether that be a project representing a binary jar,
     * or a project representing a different source module within the current application).
     */
    @Adjacency(label = TYPE + ".representedProject", direction = Direction.OUT)
    void setProject(ProjectModel projectModel);

    @Adjacency(label = TYPE + ".representedProject", direction = Direction.OUT)
    ProjectModel getProjectModel();
    
    /**
     * Sets the original {@link FileLocationModel} associated with this {@link ProjectDependencyModel}
     */
    @Adjacency(label = FILE_LOCATION_REFERENCE, direction = Direction.OUT)
    void setFileLocationReference(Iterable<FileLocationModel> m);

    /**
     * Gets the original{@link FileLocationModel} associated with this {@link ProjectDependencyModel}
     */
    @Adjacency(label = FILE_LOCATION_REFERENCE, direction = Direction.OUT)
    Iterable<FileLocationModel> getFileLocationReference();
}
