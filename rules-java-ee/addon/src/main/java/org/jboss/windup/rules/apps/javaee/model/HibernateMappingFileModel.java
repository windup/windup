package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.List;

/**
 * Contains metadata extracted from a hibernate mapping file (*.hbm.xml)
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(HibernateMappingFileModel.TYPE)
public interface HibernateMappingFileModel extends XmlFileModel {
    String TYPE = "HibernateMappingFileModel";
    String HIBERNATE_ENTITY = "hibernateEntity";
    String SPECIFICATION_VERSION = "specificationVersion";

    /**
     * Contains the specification version
     */
    @Property(SPECIFICATION_VERSION)
    String getSpecificationVersion();

    /**
     * Contains the specification version
     */
    @Property(SPECIFICATION_VERSION)
    void setSpecificationVersion(String version);

    /**
     * Contains the list of all {@link HibernateEntityModel}s referenced by this mapping file
     */
    @Adjacency(label = HIBERNATE_ENTITY, direction = Direction.OUT)
    List<HibernateEntityModel> getHibernateEntities();

    /**
     * Contains the list of all {@link HibernateEntityModel}s referenced by this mapping file
     */
    @Adjacency(label = HIBERNATE_ENTITY, direction = Direction.OUT)
    void addHibernateEntity(HibernateEntityModel hibernateEntity);
}
