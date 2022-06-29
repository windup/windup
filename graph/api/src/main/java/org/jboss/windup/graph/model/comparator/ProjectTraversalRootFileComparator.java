package org.jboss.windup.graph.model.comparator;

import java.util.Comparator;

import org.jboss.windup.graph.traversal.ProjectModelTraversal;

/**
 * Returns a comparison based on an ascending alphabetical sort of the RootFileModel's FilePath.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ProjectTraversalRootFileComparator implements Comparator<ProjectModelTraversal> {
    // Use the file path comparator in FilePathComparator
    FilePathComparator filePathComparator = new FilePathComparator();

    @Override
    public int compare(ProjectModelTraversal o1, ProjectModelTraversal o2) {
        String filePath1 = o1.getFilePath(o1.getCanonicalProject().getRootFileModel());
        String filePath2 = o2.getFilePath(o2.getCanonicalProject().getRootFileModel());
        return filePathComparator.compare(filePath1, filePath2);
    }
}
