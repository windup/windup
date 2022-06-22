package org.jboss.windup.rules.apps.javaee.model;


import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

import java.util.List;

/**
 * Contains metadata associated with a JPA Entity
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(JPAEntityModel.TYPE)
public interface JPAEntityModel extends PersistenceEntityModel {
    String TYPE = "JPAEntityModel";
    String CATALOG_NAME = TYPE + "-catalogName";
    String SCHEMA_NAME = TYPE + "-schemaName";
    String NAMED_QUERY = TYPE + "-namedQuery";
    String SPECIFICATION_VERSION = TYPE + "-specificationVersion";


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


    /**
     * Contains the jpa named query
     */
    @Adjacency(label = NAMED_QUERY, direction = Direction.OUT)
    void addNamedQuery(JPANamedQueryModel model);

    /**
     * Contains the jpa named query
     */
    @Adjacency(label = NAMED_QUERY, direction = Direction.OUT)
    List<JPANamedQueryModel> getNamedQueries();
}
