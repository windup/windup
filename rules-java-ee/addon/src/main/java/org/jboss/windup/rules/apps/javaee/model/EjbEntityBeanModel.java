package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

/**
 * Contains EJB Entity information and related data.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(EjbEntityBeanModel.TYPE)
public interface EjbEntityBeanModel extends EjbBeanBaseModel, PersistenceEntityModel {
    public static final String TYPE = "EjbEntityBeanModel";

    public static final String EJB_HOME = TYPE + "-ejbHome";
    public static final String EJB_LOCAL_HOME = TYPE + "-ejbLocalHome";
    public static final String EJB_REMOTE = TYPE + "-ejbRemote";
    public static final String EJB_LOCAL = TYPE + "-ejbLocal";
    public static final String PERSISTENCE_TYPE = TYPE + "-persistenceType";

    /**
     * Contains the Entity Persistence Type
     */
    @Property(PERSISTENCE_TYPE)
    public String getPersistenceType();

    /**
     * Contains the Entity Persistence Type
     */
    @Property(PERSISTENCE_TYPE)
    public void setPersistenceType(String persistenceType);

    /**
     * Contains the Session bean's local interface
     */
    @Adjacency(label = EJB_LOCAL, direction = Direction.OUT)
    public JavaClassModel getEjbLocal();

    /**
     * Contains the Session bean's local interface
     */
    @Adjacency(label = EJB_LOCAL, direction = Direction.OUT)
    public void setEjbLocal(JavaClassModel ejbLocal);

    /**
     * Contains the Session bean's remote interface
     */
    @Adjacency(label = EJB_REMOTE, direction = Direction.OUT)
    public JavaClassModel getEjbRemote();

    /**
     * Contains the Session bean's remote interface
     */
    @Adjacency(label = EJB_REMOTE, direction = Direction.OUT)
    public void setEjbRemote(JavaClassModel ejbRemote);

    /**
     * Contains the Session bean's local home
     */
    @Adjacency(label = EJB_LOCAL_HOME, direction = Direction.OUT)
    public JavaClassModel getEjbLocalHome();

    /**
     * Contains the Session bean's local home
     */
    @Adjacency(label = EJB_LOCAL_HOME, direction = Direction.OUT)
    public void setEjbLocalHome(JavaClassModel ejbLocalHome);

    /**
     * Contains the Session bean's home interface
     */
    @Adjacency(label = EJB_HOME, direction = Direction.OUT)
    public JavaClassModel getEjbHome();

    /**
     * Contains the Session bean's home interface
     */
    @Adjacency(label = EJB_HOME, direction = Direction.OUT)
    public void setEjbHome(JavaClassModel ejbHome);

    /**
     * References the Deployment Descriptor containing EJB.
     */
    @Adjacency(label = EjbDeploymentDescriptorModel.EJB_ENTITY_BEAN, direction = Direction.IN)
    public EjbDeploymentDescriptorModel getEjbDeploymentDescriptor();

}
