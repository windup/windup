package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
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
    public static final String PROPERTY_PROJECT_TYPE = "projectType";

    /**
     * This represents the root directory (in the case of a source-based analysis) or root archive (for binary analysis)
     * containing this particular project.
     * 
     */
    @Adjacency(label = "rootFileModel", direction = Direction.OUT)
    public void setRootFileModel(FileModel fileModel);

    @Adjacency(label = "rootFileModel", direction = Direction.OUT)
    public FileModel getRootFileModel();

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
    @Adjacency(label = "parentProject", direction = Direction.OUT)
    public void setParentProject(ProjectModel maven);

    @Adjacency(label = "parentProject", direction = Direction.OUT)
    public ProjectModel getParentProject();

    /**
     * A list of child projects
     */
    @Adjacency(label = "parentProject", direction = Direction.IN)
    public void addChildProject(ProjectModel maven);

    @Adjacency(label = "parentProject", direction = Direction.IN)
    public Iterable<ProjectModel> getChildProjects();

    /**
     * Project dependencies, as well as metadata about those deps.
     */
    @Adjacency(label = "dependency", direction = Direction.OUT)
    public void addDependency(ProjectDependency maven);

    @Adjacency(label = "dependency", direction = Direction.OUT)
    public Iterable<ProjectDependency> getDependencies();

    @Adjacency(label = "projectModelToFile", direction = Direction.OUT)
    public Iterable<FileModel> getFileModels();

    @Adjacency(label = "projectModelToFile", direction = Direction.OUT)
    public void addFileModel(FileModel fileModel);

    /**
     * Gets all contained files that are not directories
     */
    @GremlinGroovy("it.out('projectModelToFile').has('isDirectory', false)")
    public Iterable<FileModel> getFileModelsNoDirectories();
}
