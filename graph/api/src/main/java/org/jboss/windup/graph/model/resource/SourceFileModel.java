package org.jboss.windup.graph.model.resource;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Indicates that a file is source code (as opposed to a binary file of some kind).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(SourceFileModel.TYPE)
public interface SourceFileModel extends WindupVertexFrame
{
    String TYPE = "SourceFileModel";
    String TRANSFORMED_TO = "transformedTo";
    String GENERATE_SOURCE_REPORT = "generateSourceReport";

    /**
     * Links to the files that were created by transforming this file.
     */
    @Adjacency(label = TRANSFORMED_TO, direction = Direction.OUT)
    Iterable<LinkModel> getLinksToTransformedFiles();

    /**
     * Add a link to a file that was created by transforming this file.
     */
    @Adjacency(label = TRANSFORMED_TO, direction = Direction.OUT)
    void addLinkToTransformedFile(LinkModel link);

    /**
     * Contains a boolean indicating that the reporting system should generate a source report for this {@link SourceFileModel}.
     */
    @JavaHandler
    boolean isGenerateSourceReport();

    /**
     * Contains a boolean indicating that the reporting system should generate a source report for this {@link SourceFileModel}.
     */
    @Property(GENERATE_SOURCE_REPORT)
    void setGenerateSourceReport(boolean generateSourceReport);

    abstract class Impl implements SourceFileModel, JavaHandlerContext<Vertex>
    {
        @Override
        public boolean isGenerateSourceReport()
        {
            Boolean result = it().getProperty(GENERATE_SOURCE_REPORT);
            return result == null ? false : result;
        }
    }
}
