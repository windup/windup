package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata used by all specializations of EJBs (eg, environment references)
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
@TypeValue(EjbBeanBaseModel.TYPE)
public interface EjbBeanBaseModel extends WindupVertexFrame
{
    public static final String TYPE = "EjbBeanBaseModel";

    public static final String EJB_SESSION_TO_ENVIRONMENT_REFERENCE = "ejbToEnvironmentReference";
    public static final String EJB_IMPLEMENTATION_CLASS = "ejbImplementationClass";
    public static final String DISPLAY_NAME = "displayName";
    public static final String EJB_ID = "ejbId";
    public static final String TRANSACTION_TYPE = "transactionType";
    public static final String SESSION_TYPE = "sessionType";
    public static final String EJB_BEAN_NAME = "ejbBeanName";

    /**
     * Contains the bean's display name
     */
    @Property(DISPLAY_NAME)
    public String getDisplayName();

    /**
     * Contains the bean's display name
     */
    @Property(DISPLAY_NAME)
    public void setDisplayName(String displayName);

    /**
     * Contains the bean's ejb id
     */
    @Property(EJB_ID)
    public String getEjbId();

    /**
     * Contains the bean's ejb id
     */
    @Property(EJB_ID)
    public void setEjbId(String id);

    /**
     * Contains the bean's type
     */
    @Property(SESSION_TYPE)
    public String getSessionType();

    /**
     * Contains the bean's type
     */
    @Property(SESSION_TYPE)
    public void setSessionType(String sessionType);

    /**
     * Contains the bean's transaction type
     */
    @Property(TRANSACTION_TYPE)
    public String getTransactionType();

    /**
     * Contains the bean's transaction type
     */
    @Property(TRANSACTION_TYPE)
    public void setTransactionType(String transactionType);

    /**
     * Contains the bean's name
     */
    @Property(EJB_BEAN_NAME)
    String getBeanName();

    /**
     * Contains the bean's name
     */
    @Property(EJB_BEAN_NAME)
    public void setBeanName(String ejbSessionBeanName);

    /**
     * Contains the bean's implementation class
     */
    @Adjacency(label = EJB_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public void setEjbClass(JavaClassModel ejbHome);

    /**
     * Contains the bean's implementation class
     */
    @Adjacency(label = EJB_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    public JavaClassModel getEjbClass();

    /**
     * Maintains a list of {@link EnvironmentReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = EJB_SESSION_TO_ENVIRONMENT_REFERENCE, direction = Direction.OUT)
    public Iterable<EnvironmentReferenceModel> getEnvironmentReferences();

    /**
     * Maintains a list of {@link EnvironmentReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = EJB_SESSION_TO_ENVIRONMENT_REFERENCE, direction = Direction.OUT)
    public void addEnvironmentReference(EnvironmentReferenceModel environmentReference);
}
