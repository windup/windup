package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Indexed;

import com.syncleus.ferma.annotations.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a data source within the application.
 */
@TypeValue(DataSourceModel.TYPE)
public interface DataSourceModel extends JNDIResourceModel
{
    public static final String TYPE = "DataSourceModel";

    public static final String NAME = TYPE + "-name";
    public static final String DATABASE_TYPE_NAME = "databaseTypeName";
    public static final String DATABASE_TYPE_VERSION = "databaseTypeVersion";
    public static final String IS_XA = "isXA";

    /**
     * Name of the datasource.
     */
    @Indexed
    @Property(NAME)
    public String getName();

    /**
     * Name of the datasource.
     */
    @Property(NAME)
    public void setName(String name);


    /**
     * Defines whether it is an XA datasource.
     */
    @Property(IS_XA)
    public Boolean getXa();

    /**
     * Defines whether it is an XA datasource.
     */
    @Property(IS_XA)
    public void setXa(Boolean isXa);


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
