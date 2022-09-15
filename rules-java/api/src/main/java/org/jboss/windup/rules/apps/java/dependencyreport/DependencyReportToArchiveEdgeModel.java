package org.jboss.windup.rules.apps.java.dependencyreport;

import com.syncleus.ferma.annotations.InVertex;
import com.syncleus.ferma.annotations.OutVertex;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupEdgeFrame;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

/**
 * @author mnovotny
 */
@TypeValue(DependencyReportToArchiveEdgeModel.TYPE)
public interface DependencyReportToArchiveEdgeModel extends WindupEdgeFrame {
    String TYPE = "DependencyReportToArchiveEdgeModel";

    String FULL_PATH = "fullPath";

    /**
     * Returns the full path to the root of the application.
     */
    @Property(FULL_PATH)
    String getFullPath();

    /**
     * Sets the full path to the root of the application.
     */
    @Property(FULL_PATH)
    void setFullPath(String path);

    /**
     * Contains the link to the group of archives that all have the same SHA1 hash.
     */
    @OutVertex
    DependencyReportDependencyGroupModel getDependencyGroup();

    /**
     * Contains a link to the archive.
     */
    @InVertex
    ArchiveModel getArchive();
}
