package org.jboss.windup.rules.apps.javaee.model.stats;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.util.Date;
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
    

    String STATS_FILES_BYTYPE_JAVA_PERCENT = "stats.files.byType.java.percent";
    @Property(STATS_FILES_BYTYPE_JAVA_PERCENT) int getStatsFilesByTypeJavaPercent();
    @Property(STATS_FILES_BYTYPE_JAVA_PERCENT) TechnologiesStatsModel setStatsFilesByTypeJavaPercent(int qty);

    String STATS_FILES_BYTYPE_HTML_PERCENT = "stats.files.byType.html.percent";
    @Property(STATS_FILES_BYTYPE_HTML_PERCENT) int getStatsFilesByTypeHtmlPercent();
    @Property(STATS_FILES_BYTYPE_HTML_PERCENT) TechnologiesStatsModel setStatsFilesByTypeHtmlPercent(int qty);

    String STATS_FILES_BYTYPE_XML_PERCENT = "stats.files.byType.xml.percent";
    @Property(STATS_FILES_BYTYPE_XML_PERCENT) int getStatsFilesByTypeXmlPercent();
    @Property(STATS_FILES_BYTYPE_XML_PERCENT) TechnologiesStatsModel setStatsFilesByTypeXmlPercent(int qty);

    String STATS_FILES_BYTYPE_FMT_PERCENT = "stats.files.byType.fmt.percent";
    @Property(STATS_FILES_BYTYPE_FMT_PERCENT) int getStatsFilesByTypeFmtPercent();
    @Property(STATS_FILES_BYTYPE_FMT_PERCENT) TechnologiesStatsModel setStatsFilesByTypeFmtPercent(int qty);

    String STATS_FILES_BYTYPE_JS_PERCENT = "stats.files.byType.js.percent";
    @Property(STATS_FILES_BYTYPE_JS_PERCENT) int getStatsFilesByTypeJsPercent();
    @Property(STATS_FILES_BYTYPE_JS_PERCENT) TechnologiesStatsModel setStatsFilesByTypeJsPercent(int qty);

    String STATS_FILES_BYTYPE_CSS_PERCENT = "stats.files.byType.css.percent";
    @Property(STATS_FILES_BYTYPE_CSS_PERCENT) int getStatsFilesByTypeCssPercent();
    @Property(STATS_FILES_BYTYPE_CSS_PERCENT) TechnologiesStatsModel setStatsFilesByTypeCssPercent(int qty);

    String STATS_SERVICES_EJB_STATELESS = "stats.services.ejb.stateless";
    @Property(STATS_SERVICES_EJB_STATELESS) int getStatsServicesEjbStateless();
    @Property(STATS_SERVICES_EJB_STATELESS) TechnologiesStatsModel setStatsServicesEjbStateless(int qty);

    String STATS_SERVICES_EJB_STATEFUL = "stats.services.ejb.stateful";
    @Property(STATS_SERVICES_EJB_STATEFUL) int getStatsServicesEjbStateful();
    @Property(STATS_SERVICES_EJB_STATEFUL) TechnologiesStatsModel setStatsServicesEjbStateful(int qty);

    String STATS_SERVICES_EJB_MESSAGEDRIVEN = "stats.services.ejb.messageDriven";
    @Property(STATS_SERVICES_EJB_MESSAGEDRIVEN) int getStatsServicesEjbMessageDriven();
    @Property(STATS_SERVICES_EJB_MESSAGEDRIVEN) TechnologiesStatsModel setStatsServicesEjbMessageDriven(int qty);

    String STATS_SERVICES_HTTP_JAX_RS = "stats.services.http.jax-rs";
    @Property(STATS_SERVICES_HTTP_JAX_RS) int getStatsServicesHttpJaxRs();
    @Property(STATS_SERVICES_HTTP_JAX_RS) TechnologiesStatsModel setStatsServicesHttpJaxRs(int qty);

    String STATS_SERVICES_HTTP_JAX_WS = "stats.services.http.jax-ws";
    @Property(STATS_SERVICES_HTTP_JAX_WS) int getStatsServicesHttpJaxWs();
    @Property(STATS_SERVICES_HTTP_JAX_WS) TechnologiesStatsModel setStatsServicesHttpJaxWs(int qty);

    String STATS_SERVICES_JPA_PERSISTENCEUNITS = "stats.services.jpa.persistenceUnits";
    @Property(STATS_SERVICES_JPA_PERSISTENCEUNITS) int getStatsServicesJpaPersistenceUnits();
    @Property(STATS_SERVICES_JPA_PERSISTENCEUNITS) TechnologiesStatsModel setStatsServicesJpaPersistenceUnits(int qty);

    String STATS_SERVICES_JPA_NAMEDQUERIES = "stats.services.jpa.namedQueries";
    @Property(STATS_SERVICES_JPA_NAMEDQUERIES) int getStatsServicesJpaNamedQueries();
    @Property(STATS_SERVICES_JPA_NAMEDQUERIES) TechnologiesStatsModel setStatsServicesJpaNamedQueries(int qty);

    String STATS_SERVICES_JPA_ENTITITES = "stats.services.jpa.entitites";
    @Property(STATS_SERVICES_JPA_ENTITITES) int getStatsServicesJpaEntitites();
    @Property(STATS_SERVICES_JPA_ENTITITES) TechnologiesStatsModel setStatsServicesJpaEntitites(int qty);

    String STATS_SERVICES_RMI_SERVICES = "stats.services.rmi.services";
    @Property(STATS_SERVICES_RMI_SERVICES) int getStatsServicesRmiServices();
    @Property(STATS_SERVICES_RMI_SERVICES) TechnologiesStatsModel setStatsServicesRmiServices(int qty);

    String STATS_SERVERRESOURCES_DB_JDBCDATASOURCES = "stats.serverResources.db.jdbcDatasources";
    @Property(STATS_SERVERRESOURCES_DB_JDBCDATASOURCES) int getStatsServerResourcesDbJdbcDatasources();
    @Property(STATS_SERVERRESOURCES_DB_JDBCDATASOURCES) TechnologiesStatsModel setStatsServerResourcesDbJdbcDatasources(int qty);

    String STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES = "stats.serverResources.db.xaJdbcDatasources";
    @Property(STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES) int getStatsServerResourcesDbXaJdbcDatasources();
    @Property(STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES) TechnologiesStatsModel setStatsServerResourcesDbXaJdbcDatasources(int qty);

    String STATS_SERVERRESOURCES_MSG_JMS_QUEUES = "stats.serverResources.msg.jms.queues";
    @Property(STATS_SERVERRESOURCES_MSG_JMS_QUEUES) int getStatsServerResourcesMsgJmsQueues();
    @Property(STATS_SERVERRESOURCES_MSG_JMS_QUEUES) TechnologiesStatsModel setStatsServerResourcesMsgJmsQueues(int qty);

    String STATS_SERVERRESOURCES_MSG_JMS_TOPICS = "stats.serverResources.msg.jms.topics";
    @Property(STATS_SERVERRESOURCES_MSG_JMS_TOPICS) int getStatsServerResourcesMsgJmsTopics();
    @Property(STATS_SERVERRESOURCES_MSG_JMS_TOPICS) TechnologiesStatsModel setStatsServerResourcesMsgJmsTopics(int qty);

    String STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES = "stats.serverResources.msg.jms.connectionFactories";
    @Property(STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES) int getStatsServerResourcesMsgJmsConnectionFactories();
    @Property(STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES) TechnologiesStatsModel setStatsServerResourcesMsgJmsConnectionFactories(int qty);

    String STATS_SERVERRESOURCES_SECURITY_REALMS = "stats.serverResources.security.realms";
    @Property(STATS_SERVERRESOURCES_SECURITY_REALMS) int getStatsServerResourcesSecurityRealms();
    @Property(STATS_SERVERRESOURCES_SECURITY_REALMS) TechnologiesStatsModel setStatsServerResourcesSecurityRealms(int qty);

    String STATS_SERVERRESOURCES_JNDI_TOTALENTRIES = "stats.serverResources.jndi.totalEntries";
    @Property(STATS_SERVERRESOURCES_JNDI_TOTALENTRIES) int getStatsServerResourcesJndiTotalEntries();
    @Property(STATS_SERVERRESOURCES_JNDI_TOTALENTRIES) TechnologiesStatsModel setStatsServerResourcesJndiTotalEntries(int qty);

    String STATS_JAVA_CLASSES_ORIGINAL = "stats.java.classes.original";
    @Property(STATS_JAVA_CLASSES_ORIGINAL) int getStatsJavaClassesOriginal();
    @Property(STATS_JAVA_CLASSES_ORIGINAL) TechnologiesStatsModel setStatsJavaClassesOriginal(int qty);

    String STATS_JAVA_JARS_ORIGINAL = "stats.java.jars.original";
    @Property(STATS_JAVA_JARS_ORIGINAL) int getStatsJavaJarsOriginal();
    @Property(STATS_JAVA_JARS_ORIGINAL) TechnologiesStatsModel setStatsJavaJarsOriginal(int qty);

    String STATS_JAVA_CLASSES_TOTAL = "stats.java.classes.total";
    @Property(STATS_JAVA_CLASSES_TOTAL) int getStatsJavaClassesTotal();
    @Property(STATS_JAVA_CLASSES_TOTAL) TechnologiesStatsModel setStatsJavaClassesTotal(int qty);

    String STATS_JAVA_JARS_TOTAL = "stats.java.jars.total";
    @Property(STATS_JAVA_JARS_TOTAL) int getStatsJavaJarsTotal();
    @Property(STATS_JAVA_JARS_TOTAL) TechnologiesStatsModel setStatsJavaJarsTotal(int qty);

}
