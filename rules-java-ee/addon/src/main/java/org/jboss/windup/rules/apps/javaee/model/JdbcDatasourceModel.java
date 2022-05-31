package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;


/**
 * Describes a general JDBC datasource, the basic propeties.
 * A datasource may be discovered in the application, for instance, JBoss -ds.xml file;
 * or, refenced from the app, but defined in server's configuration, e.g. through web-app.xml's resource-ref.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(JdbcDatasourceModel.TYPE)
public interface JdbcDatasourceModel extends WindupVertexFrame {
    String TYPE = "JdbcDatasourceModel";

    String NAME = TYPE + "-name";
    String URL = TYPE + "-url";
    String USER = TYPE + "-user";
    String PASS = TYPE + "-pass";
    String DRIVER = TYPE + "-driver";

    String CLASS = TYPE + "-type";


    /**
     * Datasource name.
     */
    @Property(NAME)
    String getName();

    /**
     * Datasource name.
     */
    @Property(NAME)
    void setName(String name);

    /**
     * Datasource URL.
     */
    @Property(URL)
    String getUrl();

    /**
     * Datasource URL.
     */
    @Property(URL)
    void setUrl(String url);

    /**
     * Datasource user name.
     */
    @Property(USER)
    String getUser();

    /**
     * Datasource user name.
     */
    @Property(USER)
    void setUser(String user);

    /**
     * Datasource password.
     */
    @Property(PASS)
    String getPassword();

    /**
     * Datasource password.
     */
    @Property(PASS)
    void setPassword(String pass);

    /**
     * Datasource JDBC driver.
     */
    @Property(DRIVER)
    String getDriver();

    /**
     * Datasource JDBC driver.
     */
    @Property(DRIVER)
    void setDriver(String driver);

}
