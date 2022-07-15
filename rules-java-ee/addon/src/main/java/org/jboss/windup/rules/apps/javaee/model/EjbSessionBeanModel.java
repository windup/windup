package org.jboss.windup.rules.apps.javaee.model;

import java.util.Map;

import org.jboss.windup.graph.MapInProperties;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

/**
 * Contains EJB Session Bean information and related data.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(EjbSessionBeanModel.TYPE)
public interface EjbSessionBeanModel extends EjbBeanBaseModel {

    String TYPE = "EjbSessionBeanModel";

    String EJB_HOME = "ejbHome";
    String EJB_LOCAL_HOME = "ejbLocalHome";
    String EJB_REMOTE = "ejbRemote";
    String EJB_LOCAL = "ejbLocal";
    String GLOBAL_JNDI = "globalJNDI";
    String MODULE_JNDI = "moduleJNDI";
    String LOCAL_JNDI = "localJNDI";
    String CLUSTERED = "clustered";
    String THREAD_POOL = "threadPool";


    /**
     * Contains the session bean clustering config
     */
    @Property(CLUSTERED)
    Boolean isClustered();

    /**
     * Contains the session bean clustering config
     */
    @Property(CLUSTERED)
    void setClustered(Boolean clustered);

    /**
     * Contains the Session bean's local interface
     */
    @Adjacency(label = EJB_LOCAL, direction = Direction.OUT)
    void setEjbLocal(JavaClassModel ejbLocal);

    /**
     * Contains the Session bean's local interface
     */
    @Adjacency(label = EJB_LOCAL, direction = Direction.OUT)
    JavaClassModel getEjbLocal();

    /**
     * Contains the Session bean's remote interface
     */
    @Adjacency(label = EJB_REMOTE, direction = Direction.OUT)
    void setEjbRemote(JavaClassModel ejbRemote);

    /**
     * Contains the Session bean's remote interface
     */
    @Adjacency(label = EJB_REMOTE, direction = Direction.OUT)
    JavaClassModel getEjbRemote();

    /**
     * Contains the Session bean's local home
     */
    @Adjacency(label = EJB_LOCAL_HOME, direction = Direction.OUT)
    void setEjbLocalHome(JavaClassModel ejbLocalHome);

    /**
     * Contains the Session bean's local home
     */
    @Adjacency(label = EJB_LOCAL_HOME, direction = Direction.OUT)
    JavaClassModel getEjbLocalHome();

    /**
     * Contains the Session bean's home interface
     */
    @Adjacency(label = EJB_HOME, direction = Direction.OUT)
    void setEjbHome(JavaClassModel ejbHome);

    /**
     * Contains the Session bean's home interface
     */
    @Adjacency(label = EJB_HOME, direction = Direction.OUT)
    JavaClassModel getEjbHome();

    /**
     * References the Deployment Descriptor containing EJB.
     */
    @Adjacency(label = EjbDeploymentDescriptorModel.EJB_SESSION_BEAN, direction = Direction.IN)
    EjbDeploymentDescriptorModel getEjbDeploymentDescriptor();

    /**
     * Contains the global jndi location for this resource.
     */
    @Adjacency(label = GLOBAL_JNDI, direction = Direction.OUT)
    void setGlobalJndiReference(JNDIResourceModel jndi);

    /**
     * Contains the module jndi location for this resource.
     */
    @Adjacency(label = MODULE_JNDI, direction = Direction.OUT)
    void setModuleJndiReference(JNDIResourceModel jndi);

    /**
     * Contains the app jndi location for this resource.
     */
    @Adjacency(label = LOCAL_JNDI, direction = Direction.OUT)
    void setLocalJndiReference(JNDIResourceModel jndi);

    /**
     * Contains the global jndi location for this resource.
     */
    @Adjacency(label = GLOBAL_JNDI, direction = Direction.OUT)
    JNDIResourceModel getGlobalJndiReference();

    /**
     * Contains the module jndi location for this resource.
     */
    @Adjacency(label = MODULE_JNDI, direction = Direction.OUT)
    JNDIResourceModel getModuleJndiReference();

    /**
     * Contains the app jndi location for this resource.
     */
    @Adjacency(label = LOCAL_JNDI, direction = Direction.OUT)
    JNDIResourceModel getLocalJndiReference();

    /**
     * Timeouts for each method pattern in seconds, * is wildcard
     */
    @MapInProperties(propertyPrefix = "txTimeouts", propertyType = Integer.class)
    Map<String, Integer> getTxTimeouts();

    /**
     * Timeouts for each method pattern, * is wildcard
     */
    @MapInProperties(propertyPrefix = "txTimeouts", propertyType = Integer.class)
    void setTxTimeouts(Map<String, Integer> map);

    /**
     * References the thread pool, if defined.
     */
    @Adjacency(label = THREAD_POOL, direction = Direction.OUT)
    void setThreadPool(ThreadPoolModel threadPool);

    /**
     * References the thread pool, if defined.
     */
    @Adjacency(label = THREAD_POOL, direction = Direction.OUT)
    ThreadPoolModel getThreadPool();
}
