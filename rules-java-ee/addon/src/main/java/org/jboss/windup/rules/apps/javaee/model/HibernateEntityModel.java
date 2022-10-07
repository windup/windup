package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

/**
 * Contains metadata associated with a Hibernate Entity
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(HibernateEntityModel.TYPE)
public interface HibernateEntityModel extends PersistenceEntityModel {
    String TYPE = "HibernateEntityModel";
    String CATALOG_NAME = "catalogName";
    String SCHEMA_NAME = "schemaName";
    String SPECIFICATION_VERSION = "specificationVersion";
    String HIBERNATE_ENTITY_CLASS = "hibernateEntityClass";


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
     * Contains the schema name
     */
    @Property(SCHEMA_NAME)
    String getSchemaName();

    /**
     * Contains the schema name
     */
    @Property(SCHEMA_NAME)
    void setSchemaName(String schemaName);

    /**
     * Contains the catalog name
     */
    @Property(CATALOG_NAME)
    String getCatalogName();

    /**
     * Contains the catalog name
     */
    @Property(CATALOG_NAME)
    void setCatalogName(String catalogName);

}
