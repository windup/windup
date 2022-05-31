/**
 *
 */
package org.jboss.windup.rules.apps.java.model.comparator;

import org.jboss.windup.graph.model.comparator.FilePathComparator;
import org.jboss.windup.rules.apps.java.dependencyreport.DependencyReportToArchiveEdgeModel;

import java.util.Comparator;

/**
 * @author mnovotny
 *
 */
public class DependencyReportToEdgeComparator implements Comparator<DependencyReportToArchiveEdgeModel> {

    // this compares paths in DependencyReportToEdgeComparator
    FilePathComparator filePathComparator = new FilePathComparator();

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(DependencyReportToArchiveEdgeModel o1, DependencyReportToArchiveEdgeModel o2) {
        String path1 = o1.getFullPath();
        String path2 = o2.getFullPath();
        return filePathComparator.compare(path1, path2);
    }

}
