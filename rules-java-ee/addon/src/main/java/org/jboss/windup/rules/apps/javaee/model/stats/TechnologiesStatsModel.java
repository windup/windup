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

    /**
     * How many stateless beans were found.
     */
    String STATS_SERVICES_EJB_STATELESS = "stats.services.ejb.stateless";

    /**
     * How many stateful beans were found.
     */
    String STATS_SERVICES_EJB_STATEFUL = "stats.services.ejb.stateful";

    /**
     * How many message driven beans were found.
     */
    String STATS_SERVICES_EJB_MESSAGEDRIVEN = "stats.services.ejb.messageDriven";

    /**
     * How many JAX-RS services were found.
     */
    String STATS_SERVICES_HTTP_JAX_RS = "stats.services.http.jax-rs";

    /**
     * How many JAX-WS services were found.
     */
    String STATS_SERVICES_HTTP_JAX_WS = "stats.services.http.jax-ws";

    /**
     * How many JPA entities were found.
     */
    String STATS_SERVICES_JPA_ENTITITES = "stats.services.jpa.entities";

    /**
     * How many JPA persistence units were found.
     */
    String STATS_SERVICES_JPA_PERSISTENCEUNITS = "stats.services.jpa.persistenceUnits";

    /**
     * How many JPA named queries were found.
     */
    String STATS_SERVICES_JPA_NAMEDQUERIES = "stats.services.jpa.namedQueries";

    /**
     * How many Hibernate configuration files were found.
     */
    String STATS_SERVICES_HIBERNATE_CONFIGURATIONFILES = "stats.services.hibernate.configurationFiles";

    /**
     * How many Hibernate entities were found.
     */
    String STATS_SERVICES_HIBERNATE_ENTITIES = "stats.services.hibernate.entities";

    /**
     * How many Hibernate mapping files were found.
     */
    String STATS_SERVICES_HIBERNATE_MAPPINGFILES = "stats.services.hibernate.mappingFiles";

    /**
     * How many Hibernate session factories were found.
     */
    String STATS_SERVICES_HIBERNATE_SESSIONFACTORIES = "stats.services.hibernate.sessionFactories";

    /**
     * How many RMI services were found.
     */
    String STATS_SERVICES_RMI_SERVICES = "stats.services.rmi.services";

    /**
     * How many JDBC datasources were found.
     */
    String STATS_SERVERRESOURCES_DB_JDBCDATASOURCES = "stats.serverResources.db.jdbcDatasources";

    /**
     * How many XA JDBC datasources were found.
     */
    String STATS_SERVERRESOURCES_DB_XAJDBCDATASOURCES = "stats.serverResources.db.xaJdbcDatasources";

    /**
     * How many JMS queues were found.
     */
    String STATS_SERVERRESOURCES_MSG_JMS_QUEUES = "stats.serverResources.msg.jms.queues";

    /**
     * How many JMS topics were found.
     */
    String STATS_SERVERRESOURCES_MSG_JMS_TOPICS = "stats.serverResources.msg.jms.topics";

    /**
     * How many JMS connection factories were found.
     */
    String STATS_SERVERRESOURCES_MSG_JMS_CONNECTIONFACTORIES = "stats.serverResources.msg.jms.connectionFactories";

    /**
     * How many security realms were found.
     */
    String STATS_SERVERRESOURCES_SECURITY_REALMS = "stats.serverResources.security.realms";

    /**
     * How many total JNDI entries were found.
     */
    String STATS_SERVERRESOURCES_JNDI_TOTALENTRIES = "stats.serverResources.jndi.totalEntries";

    /**
     * How many own Java classes were found.
     */
    String STATS_JAVA_CLASSES_ORIGINAL = "stats.java.classes.original";

    /**
     * How many own Java JARs were found.
     */
    String STATS_JAVA_JARS_ORIGINAL = "stats.java.jars.original";

    /**
     * How many total Java classes were found.
     */
    String STATS_JAVA_CLASSES_TOTAL = "stats.java.classes.total";

    /**
     * How many total Java JARs were found.
     */
    String STATS_JAVA_JARS_TOTAL = "stats.java.jars.total";


    /**
     * Technologies key
     */
    String STATS_TECHNOLOGIES = "stats.technologies";

    @Adjacency(label = STATS_TECHNOLOGIES, direction = Direction.OUT)
    Iterable<TechnologyKeyValuePairModel> getTechnologies();

    @Adjacency(label = STATS_TECHNOLOGIES, direction = Direction.OUT)
    TechnologiesStatsModel setTechnologies(Iterable<TechnologyKeyValuePairModel> properties);

    @Adjacency(label = STATS_TECHNOLOGIES, direction = Direction.OUT)
    TechnologiesStatsModel addTechnology(TechnologyKeyValuePairModel property);


    /**
     * File types key
     */
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
