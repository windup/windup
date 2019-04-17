package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.HasApplications;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.List;

/**
 * Contains metadata used by all specializations of EJBs (eg, environment references)
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
@TypeValue(EjbBeanBaseModel.TYPE)
public interface EjbBeanBaseModel extends FileReferenceModel, WindupVertexFrame, HasApplications
{
    String TYPE = "EjbBeanBaseModel";

    String EJB_SESSION_TO_ENVIRONMENT_REFERENCE = "ejbToEnvironmentReference";
    String EJB_IMPLEMENTATION_CLASS = "ejbImplementationClass";
    String DISPLAY_NAME = "displayName";
    String EJB_ID = "ejbId";
    String TRANSACTION_TYPE = "transactionType";
    String SESSION_TYPE = "sessionType";
    String EJB_BEAN_NAME = "ejbBeanName";
    String APPLICATIONS = "applications";

    /**
     * Contains the application in which this EJB was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    List<ProjectModel> getApplications();

    /**
     * Contains the application in which this EJB was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    void setApplications(Iterable<ProjectModel> applications);

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
     * Contains the {@link FileModel} referenced by this object.
     */
    @Override
    default FileModel setFile(FileModel file)
    {
        throw new UnsupportedOperationException("Please use the EjbBeanBaseModel.setEjbClass(JavaClassModel ejbHome) method instead.");
    }

    /**
     * Contains the bean's implementation class
     */
    @Adjacency(label = EJB_IMPLEMENTATION_CLASS, direction = Direction.OUT)
    JavaClassModel getEjbClass();

    /**
     * Contains the {@link FileModel} referenced by this object.
     */
    @Override
    default FileModel getFile()
    {
        return getEjbClass() != null ? getEjbClass().getSourceFile() : null;
    }

    /**
     * Maintains a list of {@link EnvironmentReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = EJB_SESSION_TO_ENVIRONMENT_REFERENCE, direction = Direction.OUT)
    List<EnvironmentReferenceModel> getEnvironmentReferences();

    /**
     * Maintains a list of {@link EnvironmentReferenceModel}s associated with this web.xml file
     */
    @Adjacency(label = EJB_SESSION_TO_ENVIRONMENT_REFERENCE, direction = Direction.OUT)
    void addEnvironmentReference(EnvironmentReferenceModel environmentReference);
}
