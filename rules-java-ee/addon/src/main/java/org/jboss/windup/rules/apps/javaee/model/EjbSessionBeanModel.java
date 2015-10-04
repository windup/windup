package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains EJB Session Bean information and related data.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(EjbSessionBeanModel.TYPE)
public interface EjbSessionBeanModel extends EjbBeanBaseModel
{

    public static final String TYPE = "EjbSessionBean";

    public static final String EJB_HOME = "ejbHome";
    public static final String EJB_LOCAL_HOME = "ejbLocalHome";
    public static final String EJB_REMOTE = "ejbRemote";
    public static final String EJB_LOCAL = "ejbLocal";
    public static final String GLOBAL_JNDI = "globalJNDI";
    public static final String MODULE_JNDI = "moduleJNDI";
    public static final String LOCAL_JNDI = "localJNDI";
    public static final String THREAD_POOL = "threadPool";

    /**
     * Contains the Session bean's local interface
     */
    @Adjacency(label = EJB_LOCAL, direction = Direction.OUT)
    public void setEjbLocal(JavaClassModel ejbLocal);

    /**
     * Contains the Session bean's local interface
     */
    @Adjacency(label = EJB_LOCAL, direction = Direction.OUT)
    public JavaClassModel getEjbLocal();

    /**
     * Contains the Session bean's remote interface
     */
    @Adjacency(label = EJB_REMOTE, direction = Direction.OUT)
    public void setEjbRemote(JavaClassModel ejbRemote);

    /**
     * Contains the Session bean's remote interface
     */
    @Adjacency(label = EJB_REMOTE, direction = Direction.OUT)
    public JavaClassModel getEjbRemote();

    /**
     * Contains the Session bean's local home
     */
    @Adjacency(label = EJB_LOCAL_HOME, direction = Direction.OUT)
    public void setEjbLocalHome(JavaClassModel ejbLocalHome);

    /**
     * Contains the Session bean's local home
     */
    @Adjacency(label = EJB_LOCAL_HOME, direction = Direction.OUT)
    public JavaClassModel getEjbLocalHome();

    /**
     * Contains the Session bean's home interface
     */
    @Adjacency(label = EJB_HOME, direction = Direction.OUT)
    public void setEjbHome(JavaClassModel ejbHome);

    /**
     * Contains the Session bean's home interface
     */
    @Adjacency(label = EJB_HOME, direction = Direction.OUT)
    public JavaClassModel getEjbHome();

    /**
     * References the Deployment Descriptor containing EJB.
     */
    @Adjacency(label = EjbDeploymentDescriptorModel.EJB_SESSION_BEAN, direction = Direction.IN)
    public EjbDeploymentDescriptorModel getEjbDeploymentDescriptor();

    /**
     * Contains the global jndi location for this resource.
     */
    @Adjacency(label = GLOBAL_JNDI, direction = Direction.OUT)
    public void setGlobalJndiReference(JNDIResourceModel jndi);

    /**
     * Contains the module jndi location for this resource.
     */
    @Adjacency(label = MODULE_JNDI, direction = Direction.OUT)
    public void setModuleJndiReference(JNDIResourceModel jndi);

    /**
     * Contains the app jndi location for this resource.
     */
    @Adjacency(label = LOCAL_JNDI, direction = Direction.OUT)
    public void setLocalJndiReference(JNDIResourceModel jndi);

    /**
     * Contains the global jndi location for this resource.
     */
    @Adjacency(label = GLOBAL_JNDI, direction = Direction.OUT)
    public JNDIResourceModel getGlobalJndiReference();

    /**
     * Contains the module jndi location for this resource.
     */
    @Adjacency(label = MODULE_JNDI, direction = Direction.OUT)
    public JNDIResourceModel getModuleJndiReference();

    /**
     * Contains the app jndi location for this resource.
     */
    @Adjacency(label = LOCAL_JNDI, direction = Direction.OUT)
    public JNDIResourceModel getLocalJndiReference();

    /**
     * References the thread pool, if defined.
     */
    @Adjacency(label = THREAD_POOL, direction = Direction.OUT)
    void setThreadPool(ThreadPoolModel threadPool);

    /**
     * References the thread pool, if defined.
     */
    @Adjacency(label = THREAD_POOL, direction = Direction.OUT)
    public ThreadPoolModel getThreadPool();

}
