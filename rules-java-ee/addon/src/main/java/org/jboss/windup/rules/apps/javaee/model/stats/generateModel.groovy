package org.jboss.windup.reporting.stats;

import java.lang.String;

/**
 * Generates the source snippets for statistics: 1) Frames model 2) Angular template.
 * 
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */

def items2 = [
    [key: "stats.files.byType.java.percent", label: "Java"],
    [key: "stats.files.byType.html.percent", label: "HTML"],
    [key: "stats.files.byType.xml.percent",  label: "XML"],
    [key: "stats.files.byType.fmt.percent",  label: "FreeMarker"],
    [key: "stats.files.byType.js.percent",   label: "JavaScript"],
    [key: "stats.files.byType.css.percent",  label: "CSS"],

    [key: "stats.services.ejb.stateless", label: "stateless"],
    [key: "stats.services.ejb.stateful", label: "stateful"],
    [key: "stats.services.ejb.messageDriven", label: "message driven"],
    [key: "stats.services.http.jax-rs", label: "JAX-RS services"],
    [key: "stats.services.http.jax-ws", label: "JAX-WS services"],
    [key: "stats.services.jpa.persistenceUnits", label: "persistence units"],
    [key: "stats.services.jpa.namedQueries", label: "JPA named queries"],
    [key: "stats.services.jpa.entitites", label: "JPA entities"],
    [key: "stats.services.rmi.services", label: "RMI services"],
    
    [key: "stats.serverResources.db.jdbcDatasources", label: "JDBC datasources"],
    [key: "stats.serverResources.db.xaJdbcDatasources", label: "XA JDBC datasources"],
    [key: "stats.serverResources.msg.jms.queues", label: "JMS queues"],
    [key: "stats.serverResources.msg.jms.topics", label: "JMS topics"],
    [key: "stats.serverResources.msg.jms.connectionFactories", label: "JMS connection factories"],
    [key: "stats.serverResources.security.realms", label: "security realms"],
    [key: "stats.serverResources.jndi.totalEntries", label: "total JNDI entries"],
    
    [key: "stats.java.classes.original", label: "own Java classes"],
    [key: "stats.java.jars.original", label: "own Java JARs"],
    [key: "stats.java.classes.total", label: "total Java classes"],
    [key: "stats.java.jars.total", label: "total Java JARs"],
];

def ADJ = false;

items2.each({item -> 
    def key = item["key"];
    String keyConst  = key.replaceAll(/[-\.]/, "_").toUpperCase();
    String keyMethod = key.replaceAll(/([-\.]\w)/, {x -> x[0][1].toUpperCase()}).capitalize();
    println "\n    String ${keyConst} = \"${key}\";"
    if (ADJ) {
        println "    @Adjacency(label = ${keyConst}, direction = Direction.OUT) GeneralStatsItemModel get${keyMethod}();"
        println "    @Adjacency(label = ${keyConst}, direction = Direction.OUT) void set${keyMethod}(GeneralStatsItemModel item);";
    } else {
        println "    @Property(${keyConst}) int get${keyMethod}();"
        println "    @Property(${keyConst}) TechnologiesStatsModel set${keyMethod}(int qty);";
    }
});

println();

items2.each({item -> 
    def key = item["key"];
    String keyConst  = key.replaceAll(/[-\.]/, "_").toUpperCase();
    String keyMethod = key.replaceAll(/([-\.]\w)/, {x -> x[0][1].toUpperCase()}).capitalize();
    println "<tr> <td>${item["label"]}</td> <td>{{variousStatsMap.get('${key}').quantity}}</td> </tr>";
});
/*
*/
