package org.jboss.windup.graph.model;

import org.jboss.windup.graph.model.resource.ResourceModel;

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
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(ProjectModel.TYPE)
public interface ProjectModel extends WindupVertexFrame
{
    String TYPE = "ProjectModel";
    String DEPENDENCY = "dependency";
    String PARENT_PROJECT = "parentProject";
    String ROOT_RESOURCE_MODEL = "rootResourceModel";
    String PROJECT_MODEL_TO_FILE = "projectModelToFile";
    String SOURCE_BASED = "sourceBased";
    String DESCRIPTION = "description";
    String ORGANIZATION = "organization";
    String URL = "url";
    String NAME = "name";
    String VERSION = "version";
    String PROJECT_TYPE = "projectType";

    /**
     * This represents the root directory (in the case of a source-based analysis) or root archive (for binary analysis) containing this particular
     * project.
     * 
     */
    @Adjacency(label = ROOT_RESOURCE_MODEL, direction = Direction.OUT)
    void setRootResourceModel(ResourceModel fileModel);

    @Adjacency(label = ROOT_RESOURCE_MODEL, direction = Direction.OUT)
    ResourceModel getRootResourceModel();

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
     * Retrieve all files contained within the project.
     */
    @Adjacency(label = PROJECT_MODEL_TO_FILE, direction = Direction.OUT)
    Iterable<ResourceModel> getResourceModels();

    /**
     * Add a file model to the project.
     */
    @Adjacency(label = PROJECT_MODEL_TO_FILE, direction = Direction.OUT)
    void addResourceModel(ResourceModel fileModel);

    /**
     * Gets all contained files that are not directories
     */
    @GremlinGroovy("it.out('" + PROJECT_MODEL_TO_FILE + "').has('" + ResourceModel.IS_DIRECTORY + "', false)")
    Iterable<ResourceModel> getResourceModelsNoDirectories();
}
