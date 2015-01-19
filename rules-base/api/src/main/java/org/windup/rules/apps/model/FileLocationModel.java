package org.windup.rules.apps.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.frames.Property;
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
    public void setLineNumber(int lineNumber);

    /**
     * Get the line number at which this {@link InlineHintModel} should appear in the designated {@link FileModel}.
     */
    @Property(LINE_NUMBER)
    public int getLineNumber();

    /**
     * Set the column number at which this {@link InlineHintModel} should appear in the designated {@link FileModel}.
     */
    @Property(COLUMN_NUMBER)
    public void setColumnNumber(int startPosition);

    /**
     * Get the column number at which this {@link InlineHintModel} should appear in the designated {@link FileModel}.
     */
    @Property(COLUMN_NUMBER)
    public int getColumnNumber();

    /**
     * Set the length of content for which this {@link InlineHintModel} should cover in the designated {@link FileModel} .
     */
    @Property(LENGTH)
    public void setLength(int length);

    /**
     * Get the length of content for which this {@link InlineHintModel} should cover in the designated {@link FileModel} .
     */
    @Property(LENGTH)
    public int getLength();

    /**
     * Gets the snippit referenced by this {@link FileLocationModel}.
     */
    @Property(SOURCE_SNIPPIT)
    public void setSourceSnippit(String source);

    /**
     * Sets the snippit referenced by this {@link FileLocationModel}.
     */
    @Property(SOURCE_SNIPPIT)
    public String getSourceSnippit();
}
