package org.jboss.windup.graph.model;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.model.resource.FileModel;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * This allows a {@link ProjectModel} to be traversed in a way that is aware of {@link DuplicateProjectModel}s.
 *
 * This should make it easier to calculate the actual path within the application without regard to where the original
 * project was actually stored.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ProjectModelTraversal
{
    private ProjectModelTraversal previous;
    private ProjectModel current;

    private ProjectModelTraversal(ProjectModelTraversal previous, ProjectModel current)
    {
        this.previous = previous;
        this.current = current;
    }

    /**
     * Creates a new {@link ProjectModelTraversal} based upon the provided {@link ProjectModel}. The {@link ProjectModel}
     * should be a "root" model (an application) rather than a subpart of an application.
     */
    public ProjectModelTraversal(ProjectModel current)
    {
        this.current = current;
    }

    /**
     * Gets all child projects of the current project.
     */
    public Iterable<ProjectModelTraversal> getChildren()
    {
        ProjectModel projectModel = getOriginalProject(this.current);

        return Iterables.transform(projectModel.getChildProjects(), new Function<ProjectModel, ProjectModelTraversal>()
        {
            @Override
            public ProjectModelTraversal apply(ProjectModel input)
            {
                return new ProjectModelTraversal(ProjectModelTraversal.this, input);
            }
        });
    }

    /**
     * Gets the path of the specified {@link FileModel} within this traversal. The file must be within the
     * current {@link ProjectModel} for this method to return an accurate path.
     */
    public String getFilePath(FileModel fileModel)
    {
        FileModel rootFileModel = getOriginalProject().getRootFileModel();

        String base = "";

        // get the path from the chain up until this project
        if (previous != null)
            base = combinePaths(base, previous.getFilePath(rootFileModel));

        // get the path of the root file within its project
        if (current.getRootFileModel().getParentFile() != null)
            base = combinePaths(base, current.getRootFileModel().getParentFile().getPrettyPathWithinProject());

        String rootFilename = rootFileModel.getFileName();
        base = combinePaths(base, rootFilename);

        String relativePath = fileModel.getPrettyPathWithinProject();
        return combinePaths(base, relativePath);
    }

    private String combinePaths(String path1, String path2)
    {
        if (StringUtils.isNotBlank(path1) && StringUtils.isNotBlank(path2))
            return path1 + "/" + path2;
        else
            return path1 + path2;
    }

    /**
     * Gets the original Project by unwrapping any {@link DuplicateProjectModel}s wrapping it.
     */
    public ProjectModel getOriginalProject()
    {
        return getOriginalProject(current);
    }

    private ProjectModel getOriginalProject(ProjectModel projectModel)
    {
        if (projectModel instanceof DuplicateProjectModel)
        {
            DuplicateProjectModel duplicate = (DuplicateProjectModel) projectModel;
            return getOriginalProject(duplicate.getOriginalProject());
        }
        else
        {
            return projectModel;
        }
    }
}
