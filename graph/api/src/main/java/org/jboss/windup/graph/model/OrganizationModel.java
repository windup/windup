package org.jboss.windup.graph.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This classifies files and provides general background information about a specific {@link ArchiveModel}. (For instance,
 * an XML file may be classified as a "XYZ Configuration File".) A {@link OrganizationModel} may also contain links to
 * additional information, or auto-translated/generated/updated versions of the source file.
 */
@TypeValue(OrganizationModel.TYPE)
public interface OrganizationModel extends WindupVertexFrame
{
    static final String TYPE = "OrganizationModel";
    static final String TYPE_PREFIX = TYPE + ":";
    static final String NAME = TYPE_PREFIX + "name";
    static final String LINKS = TYPE_PREFIX + "links";
    static final String ARCHIVE_MODEL = TYPE_PREFIX + "organizationModelToArchiveModel";

    /**
     * Add a {@link ArchiveModel} associated with this {@link OrganizationModel}.
     */
    @Adjacency(label = ARCHIVE_MODEL, direction = Direction.OUT)
    void addArchiveModel(ArchiveModel archiveModel);

    /**
     * Get the {@link ArchiveModel} associated with this {@link OrganizationModel}.
     */
    @Adjacency(label = ARCHIVE_MODEL, direction = Direction.OUT)
    Iterable<ArchiveModel> getArchiveModels();

    /**
     * Add a related {@link Link} to this {@link OrganizationModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    void addLink(LinkModel linkDecorator);

    /**
     * Get the related {@link Link} instances associated with this {@link OrganizationModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    Iterable<LinkModel> getLinks();

    /**
     * Set the description text of this {@link OrganizationModel}.
     */
    @Property(NAME)
    void setName(String name);

    /**
     * Get the description text of this {@link OrganizationModel}.
     */
    @Property(NAME)
    String getName();


}