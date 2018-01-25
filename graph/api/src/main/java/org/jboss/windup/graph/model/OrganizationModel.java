package org.jboss.windup.graph.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.List;

/**
 * This classifies files and provides general background information about a specific {@link ArchiveModel}. (For instance,
 * an XML file may be classified as a "XYZ Configuration File".) A {@link OrganizationModel} may also contain links to
 * additional information, or auto-translated/generated/updated versions of the source file.
 */
@TypeValue(OrganizationModel.TYPE)
public interface OrganizationModel extends WindupVertexFrame
{
    String TYPE = "OrganizationModel";
    String TYPE_PREFIX = TYPE + "-";
    String NAME = TYPE_PREFIX + "name";
    String LINKS = TYPE_PREFIX + "links";
    String ARCHIVE_MODEL = TYPE_PREFIX + "organizationModelToArchiveModel";

    /**
     * Add a {@link ArchiveModel} associated with this {@link OrganizationModel}.
     */
    @Adjacency(label = ARCHIVE_MODEL, direction = Direction.OUT)
    void addArchiveModel(ArchiveModel archiveModel);

    /**
     * Get the {@link ArchiveModel} associated with this {@link OrganizationModel}.
     */
    @Adjacency(label = ARCHIVE_MODEL, direction = Direction.OUT)
    List<ArchiveModel> getArchiveModels();

    /**
     * Add a related {@link Link} to this {@link OrganizationModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    void addLink(LinkModel linkDecorator);

    /**
     * Get the related {@link Link} instances associated with this {@link OrganizationModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    List<LinkModel> getLinks();

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
