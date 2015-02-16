package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains EJB Entity information and related data.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(EjbEntityBeanModel.TYPE)
public interface EjbEntityBeanModel extends EjbBeanBaseModel
{
    public static final String TYPE = "EjbEntityBean";

    public static final String EJB_HOME = "ejbHome";
    public static final String EJB_LOCAL_HOME = "ejbLocalHome";
    public static final String EJB_REMOTE = "ejbRemote";
    public static final String EJB_LOCAL = "ejbLocal";
    public static final String PERSISTENCE_TYPE = "persistenceType";
    public static final String TABLE_NAME = "tableName";

    /**
     * Contains the Entity Persistence Type
     */
    @Property(PERSISTENCE_TYPE)
    public void setPersistenceType(String persistenceType);

    /**
     * Contains the Entity Persistence Type
     */
    @Property(PERSISTENCE_TYPE)
    public String getPersistenceType();

    /**
     * Contains the name of the Table used by this Entity
     */
    @Property(TABLE_NAME)
    public void setTableName(String tableName);

    /**
     * Contains the name of the Table used by this Entity
     */
    @Property(TABLE_NAME)
    public String getTableName();

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
    @Adjacency(label = EjbDeploymentDescriptorModel.EJB_ENTITY_BEAN, direction = Direction.IN)
    public EjbDeploymentDescriptorModel getEjbDeploymentDescriptor();

}
