package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupEdgeFrame;

import com.tinkerpop.frames.InVertex;
import com.tinkerpop.frames.OutVertex;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;


/**
 * @author mnovotny
 *
 */
@TypeValue(JarDependencyReportToProjectEdgeModel.TYPE)
public interface JarDependencyReportToProjectEdgeModel extends WindupEdgeFrame
{
        String TYPE = "jarDependencyReportToProjectEdge";

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
        ArchiveModel getArchiveModel();

        @InVertex
        ProjectModel getProjectModel();
}
