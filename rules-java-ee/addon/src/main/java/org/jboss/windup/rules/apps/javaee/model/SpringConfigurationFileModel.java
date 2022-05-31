package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import java.util.List;

/**
 * Contains metadata extracted from the XML configuration file.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(SpringConfigurationFileModel.TYPE)
public interface SpringConfigurationFileModel extends XmlFileModel {
    String SPECIFICATION_VERSION = "specificationVersion";
    String TYPE = "SpringConfigurationFileModel";

    /**
     * The Spring specification version.
     */
    @Property(SPECIFICATION_VERSION)
    String getSpecificationVersion();

    /**
     * The Spring specification version.
     */
    @Property(SPECIFICATION_VERSION)
    void setSpecificationVersion(String version);

    /**
     * A list of Spring Beans defined within this Spring configuration file.
     */
    @Adjacency(label = SpringBeanModel.SPRING_CONFIGURATION, direction = Direction.OUT)
    List<SpringBeanModel> getSpringBeans();

    /**
     * A list of Spring Beans defined within this Spring configuration file.
     */
    @Adjacency(label = SpringBeanModel.SPRING_CONFIGURATION, direction = Direction.OUT)
    void addSpringBeanReference(SpringBeanModel springBean);
}
