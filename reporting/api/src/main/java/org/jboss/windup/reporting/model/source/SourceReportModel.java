package org.jboss.windup.reporting.model.source;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(SourceReportModel.TYPE)
public interface SourceReportModel extends ReportModel
{
    public static final String TYPE = "SourceReportModel";

    @Property("sourceType")
    public void setSourceType(String sourceType);

    @Property("sourceType")
    public String getSourceType();

    @Adjacency(label = "sourceReportSourceFileModel", direction = Direction.OUT)
    public void setSourceFileModel(FileModel fileModel);

    @Adjacency(label = "sourceReportSourceFileModel", direction = Direction.OUT)
    public FileModel getSourceFileModel();

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
