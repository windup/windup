package org.jboss.windup.graph.model;

import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
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
    String ROOT_FILE_MODEL = "rootFileModel";
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
    Boolean isSourceBased();

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
    Iterable<FileModel> getFileModels();

    /**
     * Add a file model to the project.
     */
    @Adjacency(label = PROJECT_MODEL_TO_FILE, direction = Direction.OUT)
    void addFileModel(FileModel fileModel);

    /**
     * Gets all contained files that are not directories
     */
    @GremlinGroovy("it.out('" + PROJECT_MODEL_TO_FILE + "').has('" + FileModel.IS_DIRECTORY + "', false)")
    Iterable<FileModel> getFileModelsNoDirectories();

    /**
     * Gets all contained files that unparsable
     */
    @GremlinGroovy("it.out('" + PROJECT_MODEL_TO_FILE + "').has('" + FileModel.PARSE_ERROR + "')")
    Iterable<FileModel> getUnparsableFiles();

    /**
     * Returns the project model that represents the whole application. If this projectModel is the root projectModel, it will return it.
     *
     * @return ProjectModel representing the whole application
     */
    @JavaHandler
    ProjectModel getRootProjectModel();

    /**
     * Returns this project model as well as all of its children, recursively.
     */
    @JavaHandler
    Set<ProjectModel> getAllProjectModels();

    abstract class Impl implements ProjectModel, JavaHandlerContext<Vertex>
    {
        @Override
        public ProjectModel getRootProjectModel()
        {
            ProjectModel projectModel = this;
            while (projectModel.getParentProject() != null)
            {
                projectModel = projectModel.getParentProject();
            }

            // reframe it to make sure that we return a proxy
            // (otherwise, it may return this method handler implementation, which will have some unexpected side effects)
            return frame(projectModel.asVertex());
        }

        @Override
        public Set<ProjectModel> getAllProjectModels()
        {
            Set<ProjectModel> result = new HashSet<>();
            result.add(frame(it(), ProjectModel.class));
            for (ProjectModel child : getChildProjects())
                result.addAll(child.getAllProjectModels());
            return result;
        }
    }
}
