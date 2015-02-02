package org.jboss.windup.rules.files.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Refers to a specific portion of a File and contains a reference to the code involved.
 */
@TypeValue(FileLocationModel.TYPE)
public interface FileLocationModel extends FileReferenceModel
{

    String TYPE = "fileLocationModel";
    String LINE_NUMBER = "lineNumber";
    String LENGTH = "length";
    String COLUMN_NUMBER = "startPosition";
    String SOURCE_SNIPPIT = "referenceSourceSnippit";

    /**
     * Set the line number at which this {@link InlineHintModel} should appear in the designated {@link FileModel}.
     */
    @Property(LINE_NUMBER)
    void setLineNumber(int lineNumber);

    /**
     * Get the line number at which this {@link InlineHintModel} should appear in the designated {@link FileModel}.
     */
    @Property(LINE_NUMBER)
    int getLineNumber();

    /**
     * Set the column number at which this {@link InlineHintModel} should appear in the designated {@link FileModel}.
     */
    @Property(COLUMN_NUMBER)
    void setColumnNumber(int startPosition);

    /**
     * Get the column number at which this {@link InlineHintModel} should appear in the designated {@link FileModel}.
     */
    @Property(COLUMN_NUMBER)
    int getColumnNumber();

    /**
     * Set the length of content for which this {@link InlineHintModel} should cover in the designated {@link FileModel} .
     */
    @Property(LENGTH)
    void setLength(int length);

    /**
     * Get the length of content for which this {@link InlineHintModel} should cover in the designated {@link FileModel} .
     */
    @Property(LENGTH)
    int getLength();

    /**
     * Gets the snippit referenced by this {@link FileLocationModel}.
     */
    @Property(SOURCE_SNIPPIT)
    void setSourceSnippit(String source);

    /**
     * Sets the snippit referenced by this {@link FileLocationModel}.
     */
    @Property(SOURCE_SNIPPIT)
    String getSourceSnippit();

    /**
     * Gets a human readable description of the location in the file
     */
    @JavaHandler
    String getDescription();

    abstract class Impl implements FileLocationModel, JavaHandlerContext<Vertex>
    {
        @Override
        public String getDescription()
        {
            return getSourceSnippit();
        }
    }
}
