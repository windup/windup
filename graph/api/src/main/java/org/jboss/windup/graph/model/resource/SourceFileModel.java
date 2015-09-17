package org.jboss.windup.graph.model.resource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.LinkModel;

/**
 * Indicates that a file is source code (as opposed to a binary file of some kind).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(SourceFileModel.TYPE)
public interface SourceFileModel extends WindupVertexFrame
{
    public static final String TYPE = "SourceFileModel";
    public static final String TRANSFORMED_TO = "transformedTo";
    public static final String GENERATE_SOURCE_REPORT = "generateSourceReport";


    /**
     * Links to the files that were created by transforming this file.
     */
    @Adjacency(label = TRANSFORMED_TO, direction = Direction.OUT)
    public Iterable<LinkModel> getLinksToTransformedFiles();

    /**
     * Add a link to a file that was created by transforming this file.
     */
    @Adjacency(label = TRANSFORMED_TO, direction = Direction.OUT)
    public void addLinkToTransformedFile(LinkModel link);


    /**
     * Contains a boolean indicating that the reporting system should generate a source report for this {@link SourceFileModel}.
     */
    @Property(GENERATE_SOURCE_REPORT)
    Boolean isGenerateSourceReport();

    /**
     * Contains a boolean indicating that the reporting system should generate a source report for this {@link SourceFileModel}.
     */
    @Property(GENERATE_SOURCE_REPORT)
    void setGenerateSourceReport(boolean generateSourceReport);
}
