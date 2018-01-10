package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.syncleus.ferma.annotations.Adjacency;
import com.syncleus.ferma.annotations.Property;

/**
 * Contains metadata extracted from the XML configuration file.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(SpringConfigurationFileModel.TYPE)
public interface SpringConfigurationFileModel extends XmlFileModel
{
    public static final String SPECIFICATION_VERSION = "specificationVersion";
    public static final String TYPE = "SpringConfigurationFileModel";

    /**
     * The Spring specification version.
     */
    @Property(SPECIFICATION_VERSION)
    public String getSpecificationVersion();

    /**
     * The Spring specification version.
     */
    @Property(SPECIFICATION_VERSION)
    public void setSpecificationVersion(String version);

    /**
     * A list of Spring Beans defined within this Spring configuration file.
     */
    @Adjacency(label = SpringBeanModel.SPRING_CONFIGURATION, direction = Direction.OUT)
    public Iterable<SpringBeanModel> getSpringBeans();

    /**
     * A list of Spring Beans defined within this Spring configuration file.
     */
    @Adjacency(label = SpringBeanModel.SPRING_CONFIGURATION, direction = Direction.OUT)
    public void addSpringBeanReference(SpringBeanModel springBean);
}
