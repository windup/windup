package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.config.Link;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This is used to classify lines within application source {@link FileModel} instances, and to provide hints and
 * related data regarding specific positions within those files.
 */
@TypeValue(InlineHintModel.TYPE)
public interface InlineHintModel extends FileLocationModel
{
    public static final String TYPE = "BlackListModel";
    public static final String HINT = "hint";
    public static final String EFFORT = "effort";
    public static final String LINKS = "links";
    public static final String FILE_LOCATION_REFERENCE = "fileLocationReference";

    /**
     * Sets the original {@link FileLocationModel} associated with this {@link InlineHintModel}
     */
    @Adjacency(label = FILE_LOCATION_REFERENCE, direction = Direction.OUT)
    public void setFileLocationReference(FileLocationModel m);

    /**
     * Gets the original{@link FileLocationModel} associated with this {@link InlineHintModel}
     */
    @Adjacency(label = FILE_LOCATION_REFERENCE, direction = Direction.OUT)
    public FileLocationModel getFileLocationReference();

    /**
     * Set the effort weight (E.g. How difficult is it to fix the issue?)
     */
    @Property(EFFORT)
    public void setEffort(int effort);

    /**
     * Get the effort weight (E.g. How difficult is it to fix the issue?)
     */
    @Property(EFFORT)
    public int getEffort();

    /**
     * Add a related {@link Link} to this {@link ClassificationModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    public void addLink(LinkModel linkDecorator);

    /**
     * Get the related {@link Link} instances associated with this {@link ClassificationModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    public Iterable<LinkModel> getLinks();

    /**
     * Set the text to be displayed within this {@link InlineHintModel} in the designated {@link FileModel}.
     */
    @Property(HINT)
    public void setHint(String hint);

    /**
     * Get the text to be displayed within this {@link InlineHintModel} in the designated {@link FileModel}.
     */
    @Property(HINT)
    public String getHint();
}
