package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Indexed;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

/**
 * Represents a data source within the application.
 */
@TypeValue(DataSourceModel.TYPE)
public interface DataSourceModel extends JNDIResourceModel
{
    String TYPE = "DataSourceModel";

    String NAME = TYPE + "-name";
    String DATABASE_TYPE_NAME = "databaseTypeName";
    String DATABASE_TYPE_VERSION = "databaseTypeVersion";
    String IS_XA = "isXA";

    /**
     * Name of the datasource.
     */
    @Indexed
    @Property(NAME)
    String getName();

    /**
     * Name of the datasource.
     */
    @Property(NAME)
    void setName(String name);


    /**
     * Defines whether it is an XA datasource.
     */
    @Property(IS_XA)
    Boolean getXa();

    /**
     * Defines whether it is an XA datasource.
     */
    @Property(IS_XA)
    void setXa(Boolean isXa);


    /**
     * Contains database type name.
     */
    @Property(DATABASE_TYPE_NAME)
    String getDatabaseTypeName();

    /**
     * Contains database type name.
     */
    @Property(DATABASE_TYPE_NAME)
    void setDatabaseTypeName(String databaseTypeName);


    /**
     * Contains database type version.
     */
    @Property(DATABASE_TYPE_VERSION)
    String getDatabaseTypeVersion();


    /**
     * Contains database type version.
     */
    @Property(DATABASE_TYPE_VERSION)
    void setDatabaseTypeVersion(String databaseTypeVersion);

}
