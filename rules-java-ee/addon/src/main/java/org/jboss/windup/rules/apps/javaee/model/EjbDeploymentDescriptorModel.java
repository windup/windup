package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.List;

/**
 * Contains metadata associated with EJB deployment descriptors.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(EjbDeploymentDescriptorModel.TYPE)
public interface EjbDeploymentDescriptorModel extends XmlFileModel {

    String MESSAGE_DRIVEN = "messageDriven";
    String EJB_ENTITY_BEAN = "ejbEntityBean";
    String EJB_SESSION_BEAN = "ejbSessionBean";
    String SPECIFICATION_VERSION = "specificationVersion";
    String TYPE = "EjbDeploymentDescriptorModel";

    /**
     * The EJB Specification Version
     */
    @Property(SPECIFICATION_VERSION)
    String getSpecificationVersion();

    /**
     * The EJB Specification Version
     */
    @Property(SPECIFICATION_VERSION)
    void setSpecificationVersion(String version);

    /**
     * EJB Session Beans
     */
    @Adjacency(label = EJB_SESSION_BEAN, direction = Direction.OUT)
    List<EjbSessionBeanModel> getEjbSessionBeans();

    /**
     * EJB Session Beans
     */
    @Adjacency(label = EJB_SESSION_BEAN, direction = Direction.OUT)
    void addEjbSessionBean(EjbSessionBeanModel ejbSessionBean);

    /**
     * EJB EntityBeans
     */
    @Adjacency(label = EJB_ENTITY_BEAN, direction = Direction.OUT)
    List<EjbEntityBeanModel> getEjbEntityBeans();

    /**
     * EJB EntityBeans
     */
    @Adjacency(label = EJB_SESSION_BEAN, direction = Direction.OUT)
    void addEjbEntityBean(EjbEntityBeanModel ejbEntityBean);

    /**
     * Message Driven Models
     */
    @Adjacency(label = MESSAGE_DRIVEN, direction = Direction.OUT)
    List<EjbMessageDrivenModel> getMessageDriven();

    /**
     * Message Driven Models
     */
    @Adjacency(label = MESSAGE_DRIVEN, direction = Direction.OUT)
    void addMessageDriven(EjbMessageDrivenModel messageDriven);

}
