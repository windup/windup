package org.jboss.windup.reporting.stats;

import java.lang.String;

/**
 * Generates the source snippets for statistics: 1) Frames model 2) Angular template.
 * 
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */

def items2 = [
    [key: "stats.files.byType.java.percent", label: "Java", clazz: "", props: [:]],
    [key: "stats.files.byType.html.percent", label: "HTML", clazz: "", props: [:]],
    [key: "stats.files.byType.xml.percent",  label: "XML", clazz: "", props: [:]],
    [key: "stats.files.byType.fmt.percent",  label: "FreeMarker", clazz: "", props: [:]],
    [key: "stats.files.byType.js.percent",   label: "JavaScript", clazz: "", props: [:]],
    [key: "stats.files.byType.css.percent",  label: "CSS", clazz: "", props: [:]],

    [key: "stats.services.ejb.stateless", label: "stateless", clazz: "EjbSessionBeanModel", props: ['EjbBeanBaseModel.SESSION_TYPE':'"stateless"']],
    [key: "stats.services.ejb.stateful", label: "stateful", clazz: "EjbSessionBeanModel", props: ["EjbBeanBaseModel.SESSION_TYPE":'"stateful"']],
    [key: "stats.services.ejb.messageDriven", label: "message driven", clazz: "EjbMessageDrivenModel", props: [:]],
    [key: "stats.services.http.jax-rs", label: "JAX-RS services", clazz: "JaxRSWebServiceModel", props: [:]],
    [key: "stats.services.http.jax-ws", label: "JAX-WS services", clazz: "JaxWSWebServiceModel", props: [:]],
    
    [key: "stats.services.jpa.entitites", label: "JPA entities", clazz: "EjbEntityBeanModel", props: [:]],
    [key: "stats.services.jpa.persistenceUnits", label: "JPA persistence units", clazz: "JPAPersistenceUnitModel", props: [:]],
    [key: "stats.services.jpa.namedQueries", label: "JPA named queries", clazz: "JPANamedQueryModel", props: [:]],
    [key: "stats.services.rmi.services", label: "RMI services", clazz: "", props: [:]],
    
    [key: "stats.serverResources.db.jdbcDatasources", label: "JDBC datasources", clazz: "", props: [:]],
    [key: "stats.serverResources.db.xaJdbcDatasources", label: "XA JDBC datasources", clazz: "", props: [:]],
    [key: "stats.serverResources.msg.jms.queues", label: "JMS queues", clazz: "JmsDestinationModel", props: ['JmsDestinationModel.DESTINATION_TYPE':'JmsDestinationType.QUEUE.name()']],
    [key: "stats.serverResources.msg.jms.topics", label: "JMS topics", clazz: "JmsDestinationModel", props: ['JmsDestinationModel.DESTINATION_TYPE':'JmsDestinationType.TOPIC.name()']],
    [key: "stats.serverResources.msg.jms.connectionFactories", label: "JMS connection factories", clazz: "", props: [:]],
    [key: "stats.serverResources.security.realms", label: "security realms", clazz: "", props: [:]],
    [key: "stats.serverResources.jndi.totalEntries", label: "total JNDI entries", clazz: "", props: [:]],
    
    [key: "stats.java.classes.original", label: "own Java classes", clazz: "JavaClassModel", props: [:]],
    [key: "stats.java.jars.original", label: "own Java JARs", clazz: "JarArchiveModel", props: [:]],
    [key: "stats.java.classes.total", label: "total Java classes", clazz: "JavaClassModel", props: [:]],
    [key: "stats.java.jars.total", label: "total Java JARs", clazz: "JarArchiveModel", props: [:]],
];

def ADJ = false;

items2.each({item -> 
    println ""
    println "      put(\"${item.key}\", item(\"${item.label}\", \"${item.clazz}\", new HashMap<String, String>{{";
    def Map<String, String> props = item["props"];
    props.each{k,v -> println "          put(${k}, ${v});"};
    println "      }}));";

});

items2.each({item -> 
    def key = item["key"];
    String keyConst  = key.replaceAll(/[-\.]/, "_").toUpperCase();
    String keyMethod = key.replaceAll(/([-\.]\w)/, {x -> x[0][1].toUpperCase()}).capitalize();
    println "\n    String ${keyConst} = \"${key}\";"
    if (ADJ) {
        println "    @Adjacency(label = ${keyConst}, direction = Direction.OUT) GeneralStatsItemModel get${keyMethod}();"
        println "    @Adjacency(label = ${keyConst}, direction = Direction.OUT) TechnologiesStatsModel set${keyMethod}(GeneralStatsItemModel item);";
    } else {
        println "    @Property(${keyConst}) int get${keyMethod}();"
        println "    @Property(${keyConst}) TechnologiesStatsModel set${keyMethod}(int qty);";
    }
});

println();

items2.each({item -> 
    def key = item["key"];
    String keyConst  = key.replaceAll(/[-\.]/, "_").toUpperCase();
    String keyMethod = key.replaceAll(/([-\.]\w)/, {x -> x[0][1].toUpperCase()});
    //println "<tr> <td>${item["label"]}</td> <td>{{variousStatsMap.get('${key}').quantity}}</td> </tr>";
    println "<tr> <td>${item["label"]}</td> <td>{{technologiesStats?.${keyMethod}?.quantity}}</td> </tr>";
});
/*
*/
