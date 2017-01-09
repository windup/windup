package org.jboss.windup.rules.apps.javaee.model.stats;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Maps particular set of statistic items known in advance in the properties of this single model.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(TechnologiesStatsModel.TYPE)
public interface TechnologiesStatsModel extends WindupVertexFrame
{
    String TYPE = "TechnologiesStats";
    String COMPUTED = TYPE + "_computed";

    /**
     * When this statistics were computed.
     */
    @Property(COMPUTED)
    Date getComputed();

    /**
     * When this statistics were computed.
     */
    @Property(COMPUTED)
    void setComputed(Date when);

    /*
    @MapInAdjacentVertices(label = "filesStats")
    Map<String, GeneralStatsItemModel> getFilesStats();
    void setFilesStats(Map<String, GeneralStatsItemModel> filesStats);
    */

    String STATS_FILES_BYTYPE_JAVA = "stats.files.byType.java";

    /**
     * How many Java files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_JAVA, direction = Direction.OUT)
    GeneralStatsItemModel getStatsFilesByTypeJava();

    /**
     * How many Java files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_JAVA, direction = Direction.OUT)
    TechnologiesStatsModel setStatsFilesByTypeJava(GeneralStatsItemModel item);

    String STATS_FILES_BYTYPE_HTML = "stats.files.byType.html";

    /**
     * How many HTML files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_HTML, direction = Direction.OUT)
    GeneralStatsItemModel getStatsFilesByTypeHtml();

    /**
     * How many HTML files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_HTML, direction = Direction.OUT)
    TechnologiesStatsModel setStatsFilesByTypeHtml(GeneralStatsItemModel item);

    String STATS_FILES_BYTYPE_XML = "stats.files.byType.xml";

    /**
     * How many XML files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_XML, direction = Direction.OUT)
    GeneralStatsItemModel getStatsFilesByTypeXml();

    /**
     * How many XML files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_XML, direction = Direction.OUT)
    TechnologiesStatsModel setStatsFilesByTypeXml(GeneralStatsItemModel item);

    String STATS_FILES_BYTYPE_FMT = "stats.files.byType.fmt";

    /**
     * How many FreeMarker files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_FMT, direction = Direction.OUT)
    GeneralStatsItemModel getStatsFilesByTypeFmt();

    /**
     * How many FreeMarker files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_FMT, direction = Direction.OUT)
    TechnologiesStatsModel setStatsFilesByTypeFmt(GeneralStatsItemModel item);

    String STATS_FILES_BYTYPE_JS = "stats.files.byType.js";

    /**
     * How many JavaScript files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_JS, direction = Direction.OUT)
    GeneralStatsItemModel getStatsFilesByTypeJs();

    /**
     * How many JavaScript files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_JS, direction = Direction.OUT)
    TechnologiesStatsModel setStatsFilesByTypeJs(GeneralStatsItemModel item);

    String STATS_FILES_BYTYPE_CSS = "stats.files.byType.css";

    /**
     * How many CSS files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_CSS, direction = Direction.OUT)
    GeneralStatsItemModel getStatsFilesByTypeCss();

    /**
     * How many CSS files were found.
     */
    @Adjacency(label = STATS_FILES_BYTYPE_CSS, direction = Direction.OUT)
    TechnologiesStatsModel setStatsFilesByTypeCss(GeneralStatsItemModel item);

    String STATS_SERVICES_EJB_STATELESS = "stats.services.ejb.stateless";

    /**
     * How many stateless beans were found.
     */
    @Adjacency(label = STATS_SERVICES_EJB_STATELESS, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesEjbStateless();

    /**
     * How many stateless beans were found.
     */
    @Adjacency(label = STATS_SERVICES_EJB_STATELESS, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesEjbStateless(GeneralStatsItemModel item);

    String STATS_SERVICES_EJB_STATEFUL = "stats.services.ejb.stateful";

    /**
     * How many stateful beans were found.
     */
    @Adjacency(label = STATS_SERVICES_EJB_STATEFUL, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesEjbStateful();

    /**
     * How many stateful beans were found.
     */
    @Adjacency(label = STATS_SERVICES_EJB_STATEFUL, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesEjbStateful(GeneralStatsItemModel item);

    String STATS_SERVICES_EJB_MESSAGEDRIVEN = "stats.services.ejb.messageDriven";

    /**
     * How many message driven beans were found.
     */
    @Adjacency(label = STATS_SERVICES_EJB_MESSAGEDRIVEN, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesEjbMessageDriven();

    /**
     * How many message driven beans were found.
     */
    @Adjacency(label = STATS_SERVICES_EJB_MESSAGEDRIVEN, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesEjbMessageDriven(GeneralStatsItemModel item);

    String STATS_SERVICES_HTTP_JAX_RS = "stats.services.http.jax-rs";

    /**
     * How many JAX-RS services were found.
     */
    @Adjacency(label = STATS_SERVICES_HTTP_JAX_RS, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesHttpJaxRs();

    /**
     * How many JAX-RS services were found.
     */
    @Adjacency(label = STATS_SERVICES_HTTP_JAX_RS, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesHttpJaxRs(GeneralStatsItemModel item);

    String STATS_SERVICES_HTTP_JAX_WS = "stats.services.http.jax-ws";

    /**
     * How many JAX-WS services were found.
     */
    @Adjacency(label = STATS_SERVICES_HTTP_JAX_WS, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesHttpJaxWs();

    /**
     * How many JAX-WS services were found.
     */
    @Adjacency(label = STATS_SERVICES_HTTP_JAX_WS, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesHttpJaxWs(GeneralStatsItemModel item);

    String STATS_SERVICES_JPA_ENTITITES = "stats.services.jpa.entities";

    /**
     * How many JPA entities were found.
     */
    @Adjacency(label = STATS_SERVICES_JPA_ENTITITES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesJpaEntitites();

    /**
     * How many JPA entities were found.
     */
    @Adjacency(label = STATS_SERVICES_JPA_ENTITITES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesJpaEntitites(GeneralStatsItemModel item);

    String STATS_SERVICES_JPA_PERSISTENCEUNITS = "stats.services.jpa.persistenceUnits";

    /**
     * How many JPA persistence units were found.
     */
    @Adjacency(label = STATS_SERVICES_JPA_PERSISTENCEUNITS, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesJpaPersistenceUnits();

    /**
     * How many JPA persistence units were found.
     */
    @Adjacency(label = STATS_SERVICES_JPA_PERSISTENCEUNITS, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesJpaPersistenceUnits(GeneralStatsItemModel item);

    String STATS_SERVICES_JPA_NAMEDQUERIES = "stats.services.jpa.namedQueries";

    /**
     * How many JPA named queries were found.
     */
    @Adjacency(label = STATS_SERVICES_JPA_NAMEDQUERIES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesJpaNamedQueries();

    /**
     * How many JPA named queries were found.
     */
    @Adjacency(label = STATS_SERVICES_JPA_NAMEDQUERIES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesJpaNamedQueries(GeneralStatsItemModel item);



    String STATS_SERVICES_HIBERNATE_CONFIGURATIONFILES = "stats.services.hibernate.configurationFiles";

    /**
     * How many Hibernate configuration files were found.
     */
    @Adjacency(label = STATS_SERVICES_HIBERNATE_CONFIGURATIONFILES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesHibernateConfigurationFiles();

    /**
     * How many Hibernate configuration files were found.
     */
    @Adjacency(label = STATS_SERVICES_HIBERNATE_CONFIGURATIONFILES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesHibernateConfigurationFiles(GeneralStatsItemModel item);

    String STATS_SERVICES_HIBERNATE_ENTITIES = "stats.services.hibernate.entities";

    /**
     * How many Hibernate entities were found.
     */
    @Adjacency(label = STATS_SERVICES_HIBERNATE_ENTITIES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesHibernateEntities();

    /**
     * How many Hibernate entities were found.
     */
    @Adjacency(label = STATS_SERVICES_HIBERNATE_ENTITIES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesHibernateEntities(GeneralStatsItemModel item);

    String STATS_SERVICES_HIBERNATE_MAPPINGFILES = "stats.services.hibernate.mappingFiles";

    /**
     * How many Hibernate mapping files were found.
     */
    @Adjacency(label = STATS_SERVICES_HIBERNATE_MAPPINGFILES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesHibernateMappingFiles();

    /**
     * How many Hibernate mapping files were found.
     */
    @Adjacency(label = STATS_SERVICES_HIBERNATE_MAPPINGFILES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesHibernateMappingFiles(GeneralStatsItemModel item);

    String STATS_SERVICES_HIBERNATE_SESSIONFACTORIES = "stats.services.hibernate.sessionFactories";

    /**
     * How many Hibernate session factories were found.
     */
    @Adjacency(label = STATS_SERVICES_HIBERNATE_SESSIONFACTORIES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesHibernateSessionFactories();

    /**
     * How many Hibernate session factories were found.
     */
    @Adjacency(label = STATS_SERVICES_HIBERNATE_SESSIONFACTORIES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesHibernateSessionFactories(GeneralStatsItemModel item);







    String STATS_SERVICES_RMI_SERVICES = "stats.services.rmi.services";

    /**
     * How many RMI services were found.
     */
    @Adjacency(label = STATS_SERVICES_RMI_SERVICES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServicesRmiServices();

    /**
     * How many RMI services were found.
     */
    @Adjacency(label = STATS_SERVICES_RMI_SERVICES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServicesRmiServices(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_DB_JDBCDATASOURCES = "stats.serverResources.db.jdbcDatasources";

    /**
     * How many JDBC datasources were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_DB_JDBCDATASOURCES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServerResourcesDbJdbcDatasources();

    /**
     * How many JDBC datasources were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_DB_JDBCDATASOURCES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServerResourcesDbJdbcDatasources(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES = "stats.serverResources.db.xaJdbcDatasources";

    /**
     * How many XA JDBC datasources were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServerResourcesDbXaJdbcDatasources();

    /**
     * How many XA JDBC datasources were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServerResourcesDbXaJdbcDatasources(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_MSG_JMS_QUEUES = "stats.serverResources.msg.jms.queues";

    /**
     * How many JMS queues were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_QUEUES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServerResourcesMsgJmsQueues();

    /**
     * How many JMS queues were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_QUEUES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServerResourcesMsgJmsQueues(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_MSG_JMS_TOPICS = "stats.serverResources.msg.jms.topics";

    /**
     * How many JMS topics were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_TOPICS, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServerResourcesMsgJmsTopics();

    /**
     * How many JMS topics were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_TOPICS, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServerResourcesMsgJmsTopics(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES = "stats.serverResources.msg.jms.connectionFactories";

    /**
     * How many JMS connection factories were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServerResourcesMsgJmsConnectionFactories();

    /**
     * How many JMS connection factories were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServerResourcesMsgJmsConnectionFactories(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_SECURITY_REALMS = "stats.serverResources.security.realms";

    /**
     * How many security realms were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_SECURITY_REALMS, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServerResourcesSecurityRealms();

    /**
     * How many security realms were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_SECURITY_REALMS, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServerResourcesSecurityRealms(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_JNDI_TOTALENTRIES = "stats.serverResources.jndi.totalEntries";

    /**
     * How many total JNDI entries were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_JNDI_TOTALENTRIES, direction = Direction.OUT)
    GeneralStatsItemModel getStatsServerResourcesJndiTotalEntries();

    /**
     * How many total JNDI entries were found.
     */
    @Adjacency(label = STATS_SERVERRESOURCES_JNDI_TOTALENTRIES, direction = Direction.OUT)
    TechnologiesStatsModel setStatsServerResourcesJndiTotalEntries(GeneralStatsItemModel item);

    String STATS_JAVA_CLASSES_ORIGINAL = "stats.java.classes.original";

    /**
     * How many own Java classes were found.
     */
    @Adjacency(label = STATS_JAVA_CLASSES_ORIGINAL, direction = Direction.OUT)
    GeneralStatsItemModel getStatsJavaClassesOriginal();

    /**
     * How many own Java classes were found.
     */
    @Adjacency(label = STATS_JAVA_CLASSES_ORIGINAL, direction = Direction.OUT)
    TechnologiesStatsModel setStatsJavaClassesOriginal(GeneralStatsItemModel item);

    String STATS_JAVA_JARS_ORIGINAL = "stats.java.jars.original";

    /**
     * How many own Java JARs were found.
     */
    @Adjacency(label = STATS_JAVA_JARS_ORIGINAL, direction = Direction.OUT)
    GeneralStatsItemModel getStatsJavaJarsOriginal();

    /**
     * How many own Java JARs were found.
     */
    @Adjacency(label = STATS_JAVA_JARS_ORIGINAL, direction = Direction.OUT)
    TechnologiesStatsModel setStatsJavaJarsOriginal(GeneralStatsItemModel item);

    String STATS_JAVA_CLASSES_TOTAL = "stats.java.classes.total";

    /**
     * How many total Java classes were found.
     */
    @Adjacency(label = STATS_JAVA_CLASSES_TOTAL, direction = Direction.OUT)
    GeneralStatsItemModel getStatsJavaClassesTotal();

    /**
     * How many total Java classes were found.
     */
    @Adjacency(label = STATS_JAVA_CLASSES_TOTAL, direction = Direction.OUT)
    TechnologiesStatsModel setStatsJavaClassesTotal(GeneralStatsItemModel item);

    String STATS_JAVA_JARS_TOTAL = "stats.java.jars.total";

    /**
     * How many total Java JARs were found.
     */
    @Adjacency(label = STATS_JAVA_JARS_TOTAL, direction = Direction.OUT)
    GeneralStatsItemModel getStatsJavaJarsTotal();

    /**
     * How many total Java JARs were found.
     */
    @Adjacency(label = STATS_JAVA_JARS_TOTAL, direction = Direction.OUT)
    TechnologiesStatsModel setStatsJavaJarsTotal(GeneralStatsItemModel item);


    String STATS_TECHNOLOGIES = "stats.technologies";

    @Adjacency(label = STATS_TECHNOLOGIES, direction = Direction.OUT)
    Iterable<TechnologyKeyValuePairModel> getTechnologies();

    @Adjacency(label = STATS_TECHNOLOGIES, direction = Direction.OUT)
    TechnologiesStatsModel setTechnologies(Iterable<TechnologyKeyValuePairModel> properties);

    @Adjacency(label = STATS_TECHNOLOGIES, direction = Direction.OUT)
    TechnologiesStatsModel addTechnology(TechnologyKeyValuePairModel property);


    String STATS_FILE_TYPES = "stats.fileTypes";

    @Adjacency(label = STATS_FILE_TYPES, direction = Direction.OUT)
    Iterable<TechnologyKeyValuePairModel> getFileTypes();

    @Adjacency(label = STATS_FILE_TYPES, direction = Direction.OUT)
    TechnologiesStatsModel setFileTypes(Iterable<TechnologyKeyValuePairModel> properties);

    @Adjacency(label = STATS_FILE_TYPES, direction = Direction.OUT)
    TechnologiesStatsModel addFileType(TechnologyKeyValuePairModel property);

    @JavaHandler
    Map<String, Integer> getFileTypesMap();

    @JavaHandler
    Map<String, Integer> getTechnologiesMap();

    abstract class Impl implements JavaHandlerContext<Vertex>, TechnologiesStatsModel
    {
        protected Map<String, Integer> convertKeyValuePairsToMap(Iterable<TechnologyKeyValuePairModel> keyValuePairs)
        {
            Map<String, Integer> map = new HashMap<>();
            keyValuePairs.forEach(item-> map.put(item.getName(), item.getValue()));

            return map;
        }

        @Override
        public Map<String, Integer> getFileTypesMap()
        {
            return this.convertKeyValuePairsToMap(this.getFileTypes());
        }

        @Override
        public Map<String, Integer> getTechnologiesMap()
        {
            return this.convertKeyValuePairsToMap(this.getTechnologies());
        }
    }
}
