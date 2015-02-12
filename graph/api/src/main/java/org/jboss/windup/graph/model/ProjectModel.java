package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Base interface representing an abstract project model with a project name, version, type, and location on disk. Projects may be source-based or
 * binary based.
 * 
 * Additional models may extend this to support additional project types (eg, Maven-based projects).
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(ProjectModel.TYPE)
public interface ProjectModel extends WindupVertexFrame
{
    public static final String TYPE = "ProjectModel";
    public static final String DEPENDENCY = "dependency";
    public static final String PARENT_PROJECT = "parentProject";
    public static final String ROOT_FILE_MODEL = "rootFileModel";
    public static final String PROJECT_MODEL_TO_FILE = "projectModelToFile";
    public static final String SOURCE_BASED = "sourceBased";
    public static final String DESCRIPTION = "description";
    public static final String ORGANIZATION = "organization";
    public static final String URL = "url";
    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String PROJECT_TYPE = "projectType";

    /**
     * This represents the root directory (in the case of a source-based analysis) or root archive (for binary analysis) containing this particular
     * project.
     * 
     */
    @Adjacency(label = ROOT_FILE_MODEL, direction = Direction.OUT)
    void setRootFileModel(FileModel fileModel);

    @Adjacency(label = ROOT_FILE_MODEL, direction = Direction.OUT)
    FileModel getRootFileModel();

    /**
     * Indicates whether or not this is a source-based project (eg, the project provided by the user for analysis), or a binary project (eg, as part
     * of the dependencies for a Maven project, or as a binary provided by the user for analysis).
     */
    @Property(SOURCE_BASED)
    void setSourceBased(boolean sourceBased);

    /**
     * Indicates whether or not this is a source-based project (eg, the project provided by the user for analysis), or a binary project (eg, as part
     * of the dependencies for a Maven project, or as a binary provided by the user for analysis).
     */
    @Property(SOURCE_BASED)
    boolean isSourceBased();

    /**
     * The organization associated with this project.
     */
    @Property(ORGANIZATION)
    String getOrganization();

    /**
     * The organization associated with this project.
     */
    @Property(ORGANIZATION)
    void setOrganization(String organization);

    /**
     * Indicates the project's artifact type (jar, war, ear, etc)
     */
    @Property(PROJECT_TYPE)
    void setProjectType(String projectType);

    /**
     * Indicates the project's artifact type (jar, war, ear, etc)
     */
    @Property(PROJECT_TYPE)
    String getProjectType();

    /**
     * Contains the project's version.
     */
    @Property(VERSION)
    String getVersion();

    /**
     * Contains the project's version.
     */
    @Property(VERSION)
    void setVersion(String version);

    /**
     * Contains the project's name.
     */
    @Property(NAME)
    String getName();

    /**
     * Contains the project's name.
     */
    @Property(NAME)
    void setName(String name);

    /**
     * Contains the project's description.
     */
    @Property(DESCRIPTION)
    String getDescription();

    /**
     * Contains the project's description.
     */
    @Property(DESCRIPTION)
    void setDescription(String description);

    /**
     * Contains a url to the project's website.
     */
    @Property(URL)
    String getURL();

    /**
     * Contains a url to the project's website.
     */
    @Property(URL)
    void setURL(String url);

    /**
     * The parent ProjectModel, or null if no parent is present.
     */
    @Adjacency(label = PARENT_PROJECT, direction = Direction.OUT)
    void setParentProject(ProjectModel maven);

    /**
     * The parent ProjectModel, or null if no parent is present.
     */
    @Adjacency(label = PARENT_PROJECT, direction = Direction.OUT)
    ProjectModel getParentProject();

    /**
     * A list of child projects
     */
    @Adjacency(label = PARENT_PROJECT, direction = Direction.IN)
    void addChildProject(ProjectModel maven);

    /**
     * A list of child projects
     */
    @Adjacency(label = PARENT_PROJECT, direction = Direction.IN)
    Iterable<ProjectModel> getChildProjects();

    /**
     * Project dependencies, as well as metadata about those deps.
     */
    @Adjacency(label = DEPENDENCY, direction = Direction.OUT)
    void addDependency(ProjectDependencyModel maven);

    /**
     * Project dependencies, as well as metadata about those deps.
     */
    @Adjacency(label = DEPENDENCY, direction = Direction.OUT)
    Iterable<ProjectDependencyModel> getDependencies();

    /**
     * Project dependencies, as well as metadata about those deps.
     */
    @Adjacency(label = PROJECT_MODEL_TO_FILE, direction = Direction.OUT)
    Iterable<FileModel> getFileModels();

    /**
     * Project dependencies, as well as metadata about those deps.
     */
    @Adjacency(label = PROJECT_MODEL_TO_FILE, direction = Direction.OUT)
    void addFileModel(FileModel fileModel);

    /**
     * Gets all contained files that are not directories
     */
    @GremlinGroovy("it.out('" + PROJECT_MODEL_TO_FILE + "').has('" + FileModel.IS_DIRECTORY + "', false)")
    Iterable<FileModel> getFileModelsNoDirectories();
}
