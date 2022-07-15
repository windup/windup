/**
 *
 */
package org.jboss.windup.rules.apps.java.model.comparator;

import java.util.Comparator;

import org.jboss.windup.graph.model.comparator.FilePathComparator;
import org.jboss.windup.rules.apps.java.dependencyreport.DependencyReportDependencyGroupModel;

/**
 * @author mnovotny
 *
 */
public class DependencyReportGroupModelComparator implements Comparator<DependencyReportDependencyGroupModel> {

    // this compares paths in DependencyReportToEdgeComparator
    FilePathComparator filePathComparator = new FilePathComparator();

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(DependencyReportDependencyGroupModel o1, DependencyReportDependencyGroupModel o2) {
        String path1 = o1.getCanonicalProject().getName();
        ;
        String path2 = o2.getCanonicalProject().getName();
        ;
        return path1.compareTo(path2);
    }

}
