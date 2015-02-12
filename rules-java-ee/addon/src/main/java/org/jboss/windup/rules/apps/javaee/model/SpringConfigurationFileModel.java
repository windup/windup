package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata extracted from the XML configuration file.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(SpringConfigurationFileModel.TYPE)
public interface SpringConfigurationFileModel extends XmlFileModel
{
    public static final String SPRING_BEAN = "springBean";
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
    @Adjacency(label = SPRING_BEAN, direction = Direction.OUT)
    public Iterable<SpringBeanModel> getSpringBeans();

    /**
     * A list of Spring Beans defined within this Spring configuration file.
     */
    @Adjacency(label = SPRING_BEAN, direction = Direction.OUT)
    public void addSpringBeanReference(SpringBeanModel springBean);
}
