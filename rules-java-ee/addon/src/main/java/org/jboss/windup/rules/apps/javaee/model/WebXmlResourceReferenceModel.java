package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.mixin.HasTypeModel;

/**
 * Represents an &lt;resource-ref&gt; entry from a Java deployment descriptor (eg, web.xml).
 *
 * <pre>
 *
 * <![CDATA[
 * <web-app>
 * <description>MySQL Test App</description>
 * <resource-ref>
 * <description>DB Connection</description>
 * <res-ref-name>jdbc/TestDB</res-ref-name>
 * <res-type>javax.sql.DataSource</res-type>
 * <res-auth>Container</res-auth>
 * </resource-ref>
 * </web-app>
 *
 * <Resource name="jdbc/TestDB" auth="Container" type="javax.sql.DataSource"
 * maxTotal="100" maxIdle="30" maxWaitMillis="10000"
 * username="javauser" password="javadude" driverClassName="com.mysql.jdbc.Driver"
 * url="jdbc:mysql://localhost:3306/javatest"/>
 *
 * ]]></pre>
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(WebXmlResourceReferenceModel.TYPE)
public interface WebXmlResourceReferenceModel extends WindupVertexFrame, HasTypeModel {
    String TYPE = "WebXmlResourceReferenceModel";

    String RESOURCE_NAME = "resourceName";
    String RESOURCE_AUTH = "resourceAuth";
    String DESCRIPTION = "desc";

    /**
     * Contains the reference id
     */
    @Indexed
    @Property(RESOURCE_NAME)
    String getReferenceName();

    /**
     * Contains the reference id
     */
    @Property(RESOURCE_NAME)
    void setReferenceName(String resourceId);

    /**
     * The referenced resource's auth (res-auth).
     */
    @Property(RESOURCE_AUTH)
    String getResourceAuth();

    /**
     * The referenced resource's auth (res-auth).
     */
    @Property(RESOURCE_AUTH)
    void setResourceAuth(String auth);

    /**
     * Resource description
     */
    @Property(DESCRIPTION)
    String getDescription();

    /**
     * Resource description
     */
    @Property(DESCRIPTION)
    void setDescription(String desc);
}
