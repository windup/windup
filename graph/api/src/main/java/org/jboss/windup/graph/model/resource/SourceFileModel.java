package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Indicates that a file is source code (as opposed to a binary file of some kind).
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(SourceFileModel.TYPE)
public interface SourceFileModel extends WindupVertexFrame
{
    public static final String TYPE = "SourceFileModel";
    public static final String GENERATE_SOURCE_REPORT = "generateSourceReport";

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
