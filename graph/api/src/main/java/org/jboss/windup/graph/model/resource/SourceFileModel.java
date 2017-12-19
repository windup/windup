package org.jboss.windup.graph.model.resource;

import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.syncleus.ferma.annotations.Adjacency;
import com.syncleus.ferma.annotations.Property;

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
    default boolean isGenerateSourceReport()
    {
        VertexProperty result = getElement().property(GENERATE_SOURCE_REPORT);
        return result == null ? false : (Boolean)result.value();
    }

    /**
     * Contains a boolean indicating that the reporting system should generate a source report for this {@link SourceFileModel}.
     */
    @Property(GENERATE_SOURCE_REPORT)
    void setGenerateSourceReport(boolean generateSourceReport);
}
