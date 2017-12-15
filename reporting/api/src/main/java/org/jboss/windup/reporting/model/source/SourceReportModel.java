package org.jboss.windup.reporting.model.source;

import java.io.IOException;

import com.tinkerpop.frames.Incidence;
import org.apache.commons.io.IOUtils;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.ReportFileModel;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.util.exception.WindupException;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a report on a application source code file (eg, .java file, or .xml file).
 */
@TypeValue(SourceReportModel.TYPE)
public interface SourceReportModel extends ReportModel
{
    String TYPE = "SourceReportModel";
    String SOURCE_REPORT_TO_SOURCE_FILE_MODEL = "sourceReportSourceFileModel";
    String SOURCE_TYPE = "sourceType";
    String SOURCE_REPORT_TO_PROJECT_MODEL = "sourceReportToProjectModel";

    /**
     * Indicates the type of source code (for example, "java" or "xml").
     */
    @Property(SOURCE_TYPE)
    void setSourceType(String sourceType);

    /**
     * Indicates the type of source code (for example, "java" or "xml").
     */
    @Property(SOURCE_TYPE)
    String getSourceType();

    /**
     * Contains a link to the source file.
     */
    @Adjacency(label = SOURCE_REPORT_TO_SOURCE_FILE_MODEL, direction = Direction.OUT)
    void setSourceFileModel(ReportFileModel fileModel);

    /**
     * Contains a link to the source file.
     */
    @Adjacency(label = SOURCE_REPORT_TO_SOURCE_FILE_MODEL, direction = Direction.OUT)
    ReportFileModel getSourceFileModel();

    /**
     * Gets the source file contents.
     */
    @JavaHandler
    String getSourceBody();

    /**
     * Contains all {@link ProjectModel}s that contain this file, including any duplicate {@link ProjectModel}s.
     */
    @Incidence(label = SOURCE_REPORT_TO_PROJECT_MODEL, direction = Direction.OUT)
    Iterable<SourceReportToProjectEdgeModel> getProjectEdges();

    /**
     * Contains all {@link ProjectModel}s that contain this file, including any duplicate {@link ProjectModel}s.
     */
    @Incidence(label = SOURCE_REPORT_TO_PROJECT_MODEL, direction = Direction.OUT)
    SourceReportToProjectEdgeModel addProjectModel(ProjectModel projectModel);

    abstract class Impl implements SourceReportModel, JavaHandlerContext<Vertex>
    {

        @Override
        public String getSourceBody()
        {
            try
            {
                return IOUtils.toString(getSourceFileModel().asInputStream());
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to read source file: \"" + getSourceFileModel().getFilePath()
                            + "\" due to: " + e.getMessage(), e);
            }
        }
    }
}
