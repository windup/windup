package org.jboss.windup.graph.model.resource;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.List;

/**
 * Indicates that a file is source code (as opposed to a binary file of some kind).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(SourceFileModel.TYPE)
public interface SourceFileModel extends WindupVertexFrame {
    String TYPE = "SourceFileModel";
    String TRANSFORMED_TO = "transformedTo";
    String GENERATE_SOURCE_REPORT = "generateSourceReport";

    /**
     * Links to the files that were created by transforming this file.
     */
    @Adjacency(label = TRANSFORMED_TO, direction = Direction.OUT)
    List<LinkModel> getLinksToTransformedFiles();

    /**
     * Add a link to a file that was created by transforming this file.
     */
    @Adjacency(label = TRANSFORMED_TO, direction = Direction.OUT)
    void addLinkToTransformedFile(LinkModel link);

    /**
     * Contains a boolean indicating that the reporting system should generate a source report for this {@link SourceFileModel}.
     */
    default boolean isGenerateSourceReport() {
        VertexProperty result = getElement().property(GENERATE_SOURCE_REPORT);
        if (!result.isPresent())
            return false;
        return (Boolean) result.value();
    }

    /**
     * Contains a boolean indicating that the reporting system should generate a source report for this {@link SourceFileModel}.
     */
    @Property(GENERATE_SOURCE_REPORT)
    void setGenerateSourceReport(boolean generateSourceReport);
}
