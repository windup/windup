package org.jboss.windup.graph.service;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.util.PathUtil;

import java.nio.file.Path;

/**
 * Provides useful methods for querying, creating, and updating {@link ProjectModel} instances.
 */
public class ProjectService extends GraphService<ProjectModel>
{
    public static final String SHARED_LIBS_UNIQUE_ID = "<shared-libs>";
    public static final String SHARED_LIBS_APP_NAME = "Archives shared by multiple applications";
    public static final String SHARED_LIBS_FILENAME = "shared-libs";

    public ProjectService(GraphContext context)
    {
        super(context, ProjectModel.class);
    }

    /**
     * Gets a {@link ProjectModel} with the given name.
     */
    public ProjectModel getByName(String name)
    {
        return getUnique(getTypedQuery().has(ProjectModel.NAME, name));
    }

    public ProjectModel getByUniqueID(String id)
    {
        return getUnique(getTypedQuery().has(ProjectModel.UNIQUE_ID, id));
    }

    /**
     * Gets the project model used for shared libraries (libraries duplicated in multiple places within one or
     * more applications).
     */
    public ProjectModel getOrCreateSharedLibsProject()
    {
        ProjectService service = new ProjectService(getGraphContext());
        ProjectModel sharedLibsProject = service.getByUniqueID(SHARED_LIBS_UNIQUE_ID);
        if (sharedLibsProject == null)
        {
            sharedLibsProject = service.create();
            sharedLibsProject.setName(SHARED_LIBS_APP_NAME);
            sharedLibsProject.setUniqueID(SHARED_LIBS_UNIQUE_ID);
            sharedLibsProject.setProjectType(ProjectModel.TYPE_VIRTUAL);

            // attach a directory to it, as we generally assume that all projects have a location on disk
            Path archivesDirectory = WindupConfigurationService.getArchivesPath(getGraphContext());
            Path sharedLibsPath = archivesDirectory.resolve("shared-libs-" + RandomStringUtils.randomAlphabetic(6)).resolve(SHARED_LIBS_FILENAME);
            PathUtil.createDirectory(sharedLibsPath, "shared libs virtual app");

            FileModel sharedLibsFileModel = new FileService(getGraphContext()).createByFilePath(sharedLibsPath.toString());
            sharedLibsProject.setRootFileModel(sharedLibsFileModel);
            sharedLibsProject.addFileModel(sharedLibsFileModel);

            // attach this to the configuration, so that reporting treats it as a standalone app
            WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(getGraphContext());
            configuration.addInputPath(sharedLibsFileModel);
        }

        return sharedLibsProject;
    }
}
