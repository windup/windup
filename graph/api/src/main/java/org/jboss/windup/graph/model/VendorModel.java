package org.jboss.windup.graph.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This classifies files and provides general background information about a specific {@link ArchiveModel}. (For instance,
 * an XML file may be classified as a "XYZ Configuration File".) A {@link VendorModel} may also contain links to
 * additional information, or auto-translated/generated/updated versions of the source file.
 */
@TypeValue(VendorModel.TYPE)
public interface VendorModel extends WindupVertexFrame
{
    static final String TYPE = "VendorModel";
    static final String TYPE_PREFIX = TYPE + ":";
    static final String NAME = TYPE_PREFIX + "name";
    static final String LINKS = TYPE_PREFIX + "links";
    static final String ARCHIVE_MODEL = TYPE_PREFIX + "vendorModelToArchiveModel";

    /**
     * Add a {@link ArchiveModel} associated with this {@link VendorModel}.
     */
    @Adjacency(label = ARCHIVE_MODEL, direction = Direction.OUT)
    void addArchiveModel(ArchiveModel archiveModel);

    /**
     * Get the {@link ArchiveModel} associated with this {@link VendorModel}.
     */
    @Adjacency(label = ARCHIVE_MODEL, direction = Direction.OUT)
    Iterable<ArchiveModel> getArchiveModels();

    /**
     * Add a related {@link Link} to this {@link VendorModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    void addLink(LinkModel linkDecorator);

    /**
     * Get the related {@link Link} instances associated with this {@link VendorModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    Iterable<LinkModel> getLinks();

    /**
     * Set the description text of this {@link VendorModel}.
     */
    @Property(NAME)
    void setName(String name);

    /**
     * Get the description text of this {@link VendorModel}.
     */
    @Property(NAME)
    String getName();


}