package org.jboss.windup.rules.apps.java.dependencyreport;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupEdgeFrame;

import com.tinkerpop.frames.InVertex;
import com.tinkerpop.frames.OutVertex;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * @author mnovotny
 *
 */
@TypeValue(DependencyReportToArchiveEdgeModel.TYPE)
public interface DependencyReportToArchiveEdgeModel extends WindupEdgeFrame
{
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
