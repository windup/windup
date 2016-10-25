package org.jboss.windup.rules.apps.javaee.model.stats;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.util.Date;
import java.util.Map;
import org.jboss.windup.graph.MapInAdjacentVertices;
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
    
    @Property(COMPUTED)
    Date getComputed();
    @Property(COMPUTED)
    void setComputed(Date when);
    
    /*
    @MapInAdjacentVertices(label = "filesStats")
    Map<String, GeneralStatsItemModel> getFilesStats();
    void setFilesStats(Map<String, GeneralStatsItemModel> filesStats);
    */
    
    String STATS_FILES_BYTYPE_JAVA_PERCENT = "stats.files.byType.java.percent";
    @Adjacency(label = STATS_FILES_BYTYPE_JAVA_PERCENT, direction = Direction.OUT) GeneralStatsItemModel getStatsFilesByTypeJavaPercent();
    @Adjacency(label = STATS_FILES_BYTYPE_JAVA_PERCENT, direction = Direction.OUT) TechnologiesStatsModel setStatsFilesByTypeJavaPercent(GeneralStatsItemModel item);

    String STATS_FILES_BYTYPE_HTML_PERCENT = "stats.files.byType.html.percent";
    @Adjacency(label = STATS_FILES_BYTYPE_HTML_PERCENT, direction = Direction.OUT) GeneralStatsItemModel getStatsFilesByTypeHtmlPercent();
    @Adjacency(label = STATS_FILES_BYTYPE_HTML_PERCENT, direction = Direction.OUT) TechnologiesStatsModel setStatsFilesByTypeHtmlPercent(GeneralStatsItemModel item);

    String STATS_FILES_BYTYPE_XML_PERCENT = "stats.files.byType.xml.percent";
    @Adjacency(label = STATS_FILES_BYTYPE_XML_PERCENT, direction = Direction.OUT) GeneralStatsItemModel getStatsFilesByTypeXmlPercent();
    @Adjacency(label = STATS_FILES_BYTYPE_XML_PERCENT, direction = Direction.OUT) TechnologiesStatsModel setStatsFilesByTypeXmlPercent(GeneralStatsItemModel item);

    String STATS_FILES_BYTYPE_FMT_PERCENT = "stats.files.byType.fmt.percent";
    @Adjacency(label = STATS_FILES_BYTYPE_FMT_PERCENT, direction = Direction.OUT) GeneralStatsItemModel getStatsFilesByTypeFmtPercent();
    @Adjacency(label = STATS_FILES_BYTYPE_FMT_PERCENT, direction = Direction.OUT) TechnologiesStatsModel setStatsFilesByTypeFmtPercent(GeneralStatsItemModel item);

    String STATS_FILES_BYTYPE_JS_PERCENT = "stats.files.byType.js.percent";
    @Adjacency(label = STATS_FILES_BYTYPE_JS_PERCENT, direction = Direction.OUT) GeneralStatsItemModel getStatsFilesByTypeJsPercent();
    @Adjacency(label = STATS_FILES_BYTYPE_JS_PERCENT, direction = Direction.OUT) TechnologiesStatsModel setStatsFilesByTypeJsPercent(GeneralStatsItemModel item);

    String STATS_FILES_BYTYPE_CSS_PERCENT = "stats.files.byType.css.percent";
    @Adjacency(label = STATS_FILES_BYTYPE_CSS_PERCENT, direction = Direction.OUT) GeneralStatsItemModel getStatsFilesByTypeCssPercent();
    @Adjacency(label = STATS_FILES_BYTYPE_CSS_PERCENT, direction = Direction.OUT) TechnologiesStatsModel setStatsFilesByTypeCssPercent(GeneralStatsItemModel item);

    String STATS_SERVICES_EJB_STATELESS = "stats.services.ejb.stateless";
    @Adjacency(label = STATS_SERVICES_EJB_STATELESS, direction = Direction.OUT) GeneralStatsItemModel getStatsServicesEjbStateless();
    @Adjacency(label = STATS_SERVICES_EJB_STATELESS, direction = Direction.OUT) TechnologiesStatsModel setStatsServicesEjbStateless(GeneralStatsItemModel item);

    
    String STATS_SERVICES_EJB_STATEFUL = "stats.services.ejb.stateful";
    @Adjacency(label = STATS_SERVICES_EJB_STATEFUL, direction = Direction.OUT) GeneralStatsItemModel getStatsServicesEjbStateful();
    @Adjacency(label = STATS_SERVICES_EJB_STATEFUL, direction = Direction.OUT) TechnologiesStatsModel setStatsServicesEjbStateful(GeneralStatsItemModel item);

    String STATS_SERVICES_EJB_MESSAGEDRIVEN = "stats.services.ejb.messageDriven";
    @Adjacency(label = STATS_SERVICES_EJB_MESSAGEDRIVEN, direction = Direction.OUT) GeneralStatsItemModel getStatsServicesEjbMessageDriven();
    @Adjacency(label = STATS_SERVICES_EJB_MESSAGEDRIVEN, direction = Direction.OUT) TechnologiesStatsModel setStatsServicesEjbMessageDriven(GeneralStatsItemModel item);

    String STATS_SERVICES_HTTP_JAX_RS = "stats.services.http.jax-rs";
    @Adjacency(label = STATS_SERVICES_HTTP_JAX_RS, direction = Direction.OUT) GeneralStatsItemModel getStatsServicesHttpJaxRs();
    @Adjacency(label = STATS_SERVICES_HTTP_JAX_RS, direction = Direction.OUT) TechnologiesStatsModel setStatsServicesHttpJaxRs(GeneralStatsItemModel item);

    String STATS_SERVICES_HTTP_JAX_WS = "stats.services.http.jax-ws";
    @Adjacency(label = STATS_SERVICES_HTTP_JAX_WS, direction = Direction.OUT) GeneralStatsItemModel getStatsServicesHttpJaxWs();
    @Adjacency(label = STATS_SERVICES_HTTP_JAX_WS, direction = Direction.OUT) TechnologiesStatsModel setStatsServicesHttpJaxWs(GeneralStatsItemModel item);

    String STATS_SERVICES_JPA_PERSISTENCEUNITS = "stats.services.jpa.persistenceUnits";
    @Adjacency(label = STATS_SERVICES_JPA_PERSISTENCEUNITS, direction = Direction.OUT) GeneralStatsItemModel getStatsServicesJpaPersistenceUnits();
    @Adjacency(label = STATS_SERVICES_JPA_PERSISTENCEUNITS, direction = Direction.OUT) TechnologiesStatsModel setStatsServicesJpaPersistenceUnits(GeneralStatsItemModel item);

    String STATS_SERVICES_JPA_NAMEDQUERIES = "stats.services.jpa.namedQueries";
    @Adjacency(label = STATS_SERVICES_JPA_NAMEDQUERIES, direction = Direction.OUT) GeneralStatsItemModel getStatsServicesJpaNamedQueries();
    @Adjacency(label = STATS_SERVICES_JPA_NAMEDQUERIES, direction = Direction.OUT) TechnologiesStatsModel setStatsServicesJpaNamedQueries(GeneralStatsItemModel item);

    String STATS_SERVICES_JPA_ENTITITES = "stats.services.jpa.entitites";
    @Adjacency(label = STATS_SERVICES_JPA_ENTITITES, direction = Direction.OUT) GeneralStatsItemModel getStatsServicesJpaEntitites();
    @Adjacency(label = STATS_SERVICES_JPA_ENTITITES, direction = Direction.OUT) TechnologiesStatsModel setStatsServicesJpaEntitites(GeneralStatsItemModel item);

    String STATS_SERVICES_RMI_SERVICES = "stats.services.rmi.services";
    @Adjacency(label = STATS_SERVICES_RMI_SERVICES, direction = Direction.OUT) GeneralStatsItemModel getStatsServicesRmiServices();
    @Adjacency(label = STATS_SERVICES_RMI_SERVICES, direction = Direction.OUT) TechnologiesStatsModel setStatsServicesRmiServices(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_DB_JDBCDATASOURCES = "stats.serverResources.db.jdbcDatasources";
    @Adjacency(label = STATS_SERVERRESOURCES_DB_JDBCDATASOURCES, direction = Direction.OUT) GeneralStatsItemModel getStatsServerResourcesDbJdbcDatasources();
    @Adjacency(label = STATS_SERVERRESOURCES_DB_JDBCDATASOURCES, direction = Direction.OUT) TechnologiesStatsModel setStatsServerResourcesDbJdbcDatasources(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES = "stats.serverResources.db.xaJdbcDatasources";
    @Adjacency(label = STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES, direction = Direction.OUT) GeneralStatsItemModel getStatsServerResourcesDbXaJdbcDatasources();
    @Adjacency(label = STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES, direction = Direction.OUT) TechnologiesStatsModel setStatsServerResourcesDbXaJdbcDatasources(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_MSG_JMS_QUEUES = "stats.serverResources.msg.jms.queues";
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_QUEUES, direction = Direction.OUT) GeneralStatsItemModel getStatsServerResourcesMsgJmsQueues();
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_QUEUES, direction = Direction.OUT) TechnologiesStatsModel setStatsServerResourcesMsgJmsQueues(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_MSG_JMS_TOPICS = "stats.serverResources.msg.jms.topics";
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_TOPICS, direction = Direction.OUT) GeneralStatsItemModel getStatsServerResourcesMsgJmsTopics();
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_TOPICS, direction = Direction.OUT) TechnologiesStatsModel setStatsServerResourcesMsgJmsTopics(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES = "stats.serverResources.msg.jms.connectionFactories";
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES, direction = Direction.OUT) GeneralStatsItemModel getStatsServerResourcesMsgJmsConnectionFactories();
    @Adjacency(label = STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES, direction = Direction.OUT) TechnologiesStatsModel setStatsServerResourcesMsgJmsConnectionFactories(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_SECURITY_REALMS = "stats.serverResources.security.realms";
    @Adjacency(label = STATS_SERVERRESOURCES_SECURITY_REALMS, direction = Direction.OUT) GeneralStatsItemModel getStatsServerResourcesSecurityRealms();
    @Adjacency(label = STATS_SERVERRESOURCES_SECURITY_REALMS, direction = Direction.OUT) TechnologiesStatsModel setStatsServerResourcesSecurityRealms(GeneralStatsItemModel item);

    String STATS_SERVERRESOURCES_JNDI_TOTALENTRIES = "stats.serverResources.jndi.totalEntries";
    @Adjacency(label = STATS_SERVERRESOURCES_JNDI_TOTALENTRIES, direction = Direction.OUT) GeneralStatsItemModel getStatsServerResourcesJndiTotalEntries();
    @Adjacency(label = STATS_SERVERRESOURCES_JNDI_TOTALENTRIES, direction = Direction.OUT) TechnologiesStatsModel setStatsServerResourcesJndiTotalEntries(GeneralStatsItemModel item);

    String STATS_JAVA_CLASSES_ORIGINAL = "stats.java.classes.original";
    @Adjacency(label = STATS_JAVA_CLASSES_ORIGINAL, direction = Direction.OUT) GeneralStatsItemModel getStatsJavaClassesOriginal();
    @Adjacency(label = STATS_JAVA_CLASSES_ORIGINAL, direction = Direction.OUT) TechnologiesStatsModel setStatsJavaClassesOriginal(GeneralStatsItemModel item);

    String STATS_JAVA_JARS_ORIGINAL = "stats.java.jars.original";
    @Adjacency(label = STATS_JAVA_JARS_ORIGINAL, direction = Direction.OUT) GeneralStatsItemModel getStatsJavaJarsOriginal();
    @Adjacency(label = STATS_JAVA_JARS_ORIGINAL, direction = Direction.OUT) TechnologiesStatsModel setStatsJavaJarsOriginal(GeneralStatsItemModel item);

    String STATS_JAVA_CLASSES_TOTAL = "stats.java.classes.total";
    @Adjacency(label = STATS_JAVA_CLASSES_TOTAL, direction = Direction.OUT) GeneralStatsItemModel getStatsJavaClassesTotal();
    @Adjacency(label = STATS_JAVA_CLASSES_TOTAL, direction = Direction.OUT) TechnologiesStatsModel setStatsJavaClassesTotal(GeneralStatsItemModel item);

    String STATS_JAVA_JARS_TOTAL = "stats.java.jars.total";
    @Adjacency(label = STATS_JAVA_JARS_TOTAL, direction = Direction.OUT) GeneralStatsItemModel getStatsJavaJarsTotal();
    @Adjacency(label = STATS_JAVA_JARS_TOTAL, direction = Direction.OUT) TechnologiesStatsModel setStatsJavaJarsTotal(GeneralStatsItemModel item);

}
