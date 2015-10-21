package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata used by all specializations of EJBs (eg, environment references)
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
@TypeValue(EjbBeanBaseModel.TYPE)
public interface EjbBeanBaseModel extends WindupVertexFrame
{
    String TYPE = "EjbBeanBaseModel";

    String EJB_SESSION_TO_ENVIRONMENT_REFERENCE = "ejbToEnvironmentReference";
    String EJB_IMPLEMENTATION_CLASS = "ejbImplementationClass";
    String DISPLAY_NAME = "displayName";
    String EJB_ID = "ejbId";
    String TRANSACTION_TYPE = "transactionType";
    String SESSION_TYPE = "sessionType";
    String EJB_BEAN_NAME = "ejbBeanName";
    String APPLICATION = "application";

    /**
     * Contains the application in which this EJB was discovered.
     */
    @Adjacency(label = APPLICATION, direction = Direction.OUT)
    ProjectModel getApplication();

    /**
     * Contains the application in which this EJB was discovered.
     */
    @Adjacency(label = APPLICATION, direction = Direction.OUT)
    void setApplication(ProjectModel projectModel);

    /**
     * Contains the bean's display name
     */
    @Property(DISPLAY_NAME)
    String getDisplayName();

    /**
     * Contains the bean's display name
     */
    @Property(DISPLAY_NAME)
    void setDisplayName(String displayName);

    /**
     * Contains the bean's ejb id
     */
    @Property(EJB_ID)
    String getEjbId();

    /**
     * Contains the bean's ejb id
     */
    @Indexed
    @Property(EJB_ID)
    void setEjbId(String id);

    /**
     * Contains the bean's type
     */
    @Property(SESSION_TYPE)
    String getSessionType();

    /**
     * Contains the bean's type
     */
    @Property(SESSION_TYPE)
    void setSessionType(String sessionType);

    /**
     * Contains the bean's transaction type
     */
    @Property(TRANSACTION_TYPE)
    String getTransactionType();

    /**
     * Contains the bean's transaction type
     */
    @Property(TRANSACTION_TYPE)
    void setTransactionType(String transactionType);

    /**
     * Contains the bean's name
     */
    @Property(EJB_BEAN_NAME)
    String getBeanName();

    /**
     * Contains the bean's name
     */
    @Indexed
    @Property(EJB_BEAN_NAME)
    void setBeanName(String ejbSessionBeanName);

    /**
     * Contains the bean's implementation class
     */
    @Adjacency(label = EJB_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    void setEjbClass(JavaClassModel ejbHome);

    /**
     * Contains the bean's implementation class
     */
    @Adjacency(label = EJB_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    JavaClassModel getEjbClass();

    /**
     * Maintains a list of {@link EnvironmentReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = EJB_SESSION_TO_ENVIRONMENT_REFERENCE, direction = Direction.OUT)
    Iterable<EnvironmentReferenceModel> getEnvironmentReferences();

    /**
     * Maintains a list of {@link EnvironmentReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = EJB_SESSION_TO_ENVIRONMENT_REFERENCE, direction = Direction.OUT)
    void addEnvironmentReference(EnvironmentReferenceModel environmentReference);
}
