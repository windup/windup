package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.context.Context;

public class WebLogicConfig extends WindupRuleProvider
{

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        /* TODO Change to use new Hints/classifications API
        
        List<JavaClassification> classifications = new ArrayList<JavaClassification>();
        List<BlackListRegex> hints = new ArrayList<BlackListRegex>();
        
        classifications.add(new JavaClassification(getID(), "JAX-RPC Generic Handler", "javax.xml.rpc.handler.GenericHandler", 0, Types.add(TypeReferenceLocation.INHERITANCE)));
        hints.add(new BlackListRegex(getID(), "weblogic.security.Security$", "Migrate to JBoss EAP 5: org.jboss.security.SecurityAssociation", 0, Types.add(TypeReferenceLocation.TYPE)));
        hints.add(new BlackListRegex(getID(), "weblogic.security.Security$", "Migrate to JBoss EAP 6: org.jboss.security.SecurityContextAssociation", 0, Types.add(TypeReferenceLocation.TYPE)));
        hints.add(new BlackListRegex(getID(), "weblogic.security.Security.getCurrentSubject", "Migrate to JBoss EAP 6: org.jboss.security.SecurityContextAssociation.getCurrentContext().getCurrentPrincipal()", 3, Types.add(TypeReferenceLocation.METHOD)));
        hints.add(new BlackListRegex(getID(), "weblogic.security.Security.getCurrentSubject", "Migrate to JBoss EAP 5: org.jboss.security.SecurityAssociation.getPrincipal().getName()", 3, Types.add(TypeReferenceLocation.METHOD)));
        classifications.add(new JavaClassification(getID(), "Weblogic ApplicationLifecycleListener, proprietary class, must be migrated.", "weblogic.application.ApplicationLifecycleListener$", 3, Types.add(TypeReferenceLocation.INHERITANCE)));
        hints.add(new BlackListRegex(getID(), "weblogic.application.ApplicationLifecycleListener$", "This class is proprietary to Weblogic, remove.", 2, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "weblogic.application.ApplicationLifecycleListener$", "Use a javax.servlet.ServletContextListener with @javax.annotation.servlet.WebListener, or EJB 3.1 @javax.ejb.Startup @javax.ejb.Singleton service bean.", 2, Types.add(TypeReferenceLocation.INHERITANCE)));
        hints.add(new BlackListRegex(getID(), "weblogic.application.ApplicationLifecycleEvent$", "This class is proprietary to Weblogic, remove.", 2, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "weblogic.application.ApplicationLifecycleEvent$", "<![CDATA[\n" + 
            "            Using a ServletContextListener, you can use an ServletContextEvent to access the properties of the web application container.  Or, use an EJB 3.1 with annotated methods with javax.annotation.PostContruct and javax.annotation.PreDestory\n" + 
            "                    \n" + 
            "            *Example leveraging WebListener annotations:*\n" + 
            "            \n" + 
            "            ```java\n" + 
            "            @WebListener\n" + 
            "            public class ContextListener implements ServletContextListener { ... }\n" + 
            "            ```\n" + 
            "            \n" + 
            "            *Example leveraging Startup and Singleton annotations:*\n" + 
            "            \n" + 
            "            ```java\n" + 
            "            @Startup\n" + 
            "            @Singleton\n" + 
            "            public class StartupBean { ... }\n" + 
            "            ```\n" + 
            "            ]]>", 0, Types.add(TypeReferenceLocation.TYPE)));
        hints.add(new BlackListRegex(getID(), "weblogic.i18n.logging.NonCatalogLogger\\(.+\\)", "<![CDATA[\n" + 
            "            Migrate the NonCatalogLogger to Apache Log4j.\n" + 
            "    \n" + 
            "            ```java\n" + 
            "            Logger LOG = Logger.getLog(\"Example\");\n" + 
            "            ```\n" + 
            "    \n" + 
            "            ]]>", 1, Types.add(TypeReferenceLocation.CONSTRUCTOR_CALL)));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsConnectionFactory", "Migrate to: javax.jms.ConnectionFactory", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsQueueConnectionFactory", "Migrate to: javax.jms.QueueConnectionFactory", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsTopicConnectionFactory", "Migrate to: javax.jms.TopicConnectionFactory", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsDestination", "Migrate to: javax.jms.Destination", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsMessage", "Migrate to: javax.jms.Message", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsBytesMessage", "Migrate to: javax.jms.BytesMessage", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsMapMessage", "Migrate to: javax.jms.MapMessage", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsObjectMessage", "Migrate to: javax.jms.ObjectMessage", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsStreamMessage", "Migrate to: javax.jms.StreamMessage", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsTextMessage", "Migrate to: javax.jms.TextMessage", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsConnection", "Migrate to: javax.jms.Connection", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsConsumer", "Migrate to: javax.jms.MessageConsumer", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsProducer", "Migrate to: javax.jms.MessageProducer", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsQueueBrowser", "Migrate to: javax.jms.QueueBrowser", 1));
        hints.add(new BlackListRegex(getID(), "oracle.jms.AQjmsSession", "Migrate to: javax.jms.Session", 1)); 
        
        Configuration configuration = ConfigurationBuilder.begin()
            .addRule().perform(new JavaScanner().add(classifications).add(hints));
        return configuration;
        */
        return null;
    }
    // @formatter:on
}