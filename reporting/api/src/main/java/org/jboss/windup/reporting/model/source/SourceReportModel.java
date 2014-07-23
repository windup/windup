package org.jboss.windup.reporting.model.source;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.reporting.model.ReportFileModel;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a report on a application source code file (eg, .java file, or .xml file).
 * 
 */
@TypeValue(SourceReportModel.TYPE)
public interface SourceReportModel extends ReportModel
{
    public static final String TYPE = "SourceReportModel";
    public static final String SOURCE_REPORT_TO_SOURCE_FILE_MODEL = "sourceReportSourceFileModel";

    @Property("sourceType")
    public void setSourceType(String sourceType);

    @Property("sourceType")
    public String getSourceType();

    @Adjacency(label = SOURCE_REPORT_TO_SOURCE_FILE_MODEL, direction = Direction.OUT)
    public void setSourceFileModel(ReportFileModel fileModel);

    @Adjacency(label = SOURCE_REPORT_TO_SOURCE_FILE_MODEL, direction = Direction.OUT)
    public ReportFileModel getSourceFileModel();

    @JavaHandler
    public String getSourceBody();

    abstract class Impl implements SourceReportModel, JavaHandlerContext<Vertex>
    {
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
