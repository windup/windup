package org.jboss.windup.rules.apps.java.scan.model.project;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Base interface representing an abstract project model with a project name, version, type, and location on disk.
 * Projects may be source-based or binary based.
 * 
 * Additional models may extend this to support additional project types (eg, Maven-based projects).
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue("ProjectModel")
public interface ProjectModel extends WindupVertexFrame
{
    public static final String PROPERTY_SOURCE_BASED = "sourceBased";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_PROJECT_LOCATION = "projectLocation";
    public static final String PROPERTY_PROJECT_TYPE = "projectType";

    /**
     * The file (or folder) reference of this project on disk
     * 
     */
    @Property(PROPERTY_PROJECT_LOCATION)
    void setFileModel(FileModel fm);

    @Property(PROPERTY_PROJECT_LOCATION)
    FileModel getFileModel();

    /**
     * Indicates whether or not this is a source-based project (eg, the project provided by the user for analysis), or a
     * binary project (eg, as part of the dependencies for a Maven project, or as a binary provided by the user for
     * analysis).
     * 
     */
    @Property(PROPERTY_SOURCE_BASED)
    void setSourceBased(boolean sourceBased);

    @Property(PROPERTY_SOURCE_BASED)
    boolean isSourceBased();

    /**
     * Indicates the project's artifact type (jar, war, ear, etc)
     */
    @Property(PROPERTY_PROJECT_TYPE)
    void setProjectType(String projectType);

    @Property(PROPERTY_PROJECT_TYPE)
    String getProjectType();

    @Property(PROPERTY_VERSION)
    public String getVersion();

    @Property(PROPERTY_VERSION)
    public void setVersion(String version);

    @Property(PROPERTY_NAME)
    public String getName();

    @Property(PROPERTY_NAME)
    public void setName(String name);

    @Property(PROPERTY_DESCRIPTION)
    public String getDescription();

    @Property(PROPERTY_DESCRIPTION)
    public void setDescription(String description);

    /**
     * The parent ProjectModel, or null if no parent is present
     * 
     */
    @Adjacency(label = "module", direction = Direction.IN)
    public void setParent(ProjectModel maven);

    @Adjacency(label = "module", direction = Direction.IN)
    public ProjectModel getParent();

    /**
     * A list of child projects
     * 
     * @param maven
     */
    @Adjacency(label = "module", direction = Direction.OUT)
    public void addChildModule(ProjectModel maven);

    @Adjacency(label = "module", direction = Direction.OUT)
    public Iterable<ProjectModel> getChildModules();

    /**
     * Project dependencies, as well as metadata about those deps.
     */
    @Adjacency(label = "dependency", direction = Direction.OUT)
    public void addDependency(ProjectDependency maven);

    @Adjacency(label = "dependency", direction = Direction.OUT)
    public Iterable<ProjectDependency> getDependencies();
}
