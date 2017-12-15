package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata associated with EJB deployment descriptors.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
@TypeValue(EjbDeploymentDescriptorModel.TYPE)
public interface EjbDeploymentDescriptorModel extends XmlFileModel
{

    public static final String MESSAGE_DRIVEN = "messageDriven";
    public static final String EJB_ENTITY_BEAN = "ejbEntityBean";
    public static final String EJB_SESSION_BEAN = "ejbSessionBean";
    public static final String SPECIFICATION_VERSION = "specificationVersion";
    public static final String TYPE = "EjbDeploymentDescriptorModel";

    /**
     * The EJB Specification Version
     */
    @Property(SPECIFICATION_VERSION)
    public String getSpecificationVersion();

    /**
     * The EJB Specification Version
     */
    @Property(SPECIFICATION_VERSION)
    public void setSpecificationVersion(String version);

    /**
     * EJB Session Beans
     */
    @Adjacency(label = EJB_SESSION_BEAN, direction = Direction.OUT)
    public Iterable<EjbSessionBeanModel> getEjbSessionBeans();

    /**
     * EJB Session Beans
     */
    @Adjacency(label = EJB_SESSION_BEAN, direction = Direction.OUT)
    public void addEjbSessionBean(EjbSessionBeanModel ejbSessionBean);

    /**
     * EJB EntityBeans
     */
    @Adjacency(label = EJB_ENTITY_BEAN, direction = Direction.OUT)
    public Iterable<EjbEntityBeanModel> getEjbEntityBeans();

    /**
     * EJB EntityBeans
     */
    @Adjacency(label = EJB_SESSION_BEAN, direction = Direction.OUT)
    public void addEjbEntityBean(EjbEntityBeanModel ejbEntityBean);

    /**
     * Message Driven Models
     */
    @Adjacency(label = MESSAGE_DRIVEN, direction = Direction.OUT)
    public Iterable<EjbMessageDrivenModel> getMessageDriven();

    /**
     * Message Driven Models
     */
    @Adjacency(label = MESSAGE_DRIVEN, direction = Direction.OUT)
    public void addMessageDriven(EjbMessageDrivenModel messageDriven);

}
