package org.jboss.windup.graph.model.comparator;

import java.util.Comparator;

import org.jboss.windup.graph.model.ProjectModel;

/**
 * 
 * Returns a comparison based on an ascending alphabetical sort of the RootFileModel's FilePath.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class ProjectModelByRootFileComparator implements Comparator<ProjectModel>
{

    // Use the file path comparor in FilePathComparator
    FilePathComparator filePathComparator = new FilePathComparator();

    @Override
    public int compare(ProjectModel o1, ProjectModel o2)
    {
        String filePath1 = o1.getRootFileModel().getFilePath();
        String filePath2 = o2.getRootFileModel().getFilePath();
        return filePathComparator.compare(filePath1, filePath2);
    }
}
