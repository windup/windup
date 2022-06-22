package org.jboss.windup.reporting.model.source;

import com.syncleus.ferma.annotations.InVertex;
import com.syncleus.ferma.annotations.OutVertex;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupEdgeFrame;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(SourceReportToProjectEdgeModel.TYPE)
public interface SourceReportToProjectEdgeModel extends WindupEdgeFrame {
    String TYPE = "SourceReportToProjectEdgeModel";

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

    @OutVertex
    SourceReportModel getSourceReportModel();

    @InVertex
    ProjectModel getProjectModel();
}
