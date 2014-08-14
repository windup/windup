package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class WebServiceConfig extends WindupRuleProvider
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
        Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.rpc.Service$") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "JAX-RPC Service").withEffort( 0
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("weblogic.wsee.jaxrpc.ServiceImpl") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Weblogic Web Service Implementation" ).with( Link.to( "Apache CXF Simple JAX-WS Web Service Example" ,"http://cxf.apache.org/docs/a-simple-jax-ws-service.html") ) .withEffort( 6
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("weblogic.wsee.jaxrpc.ServiceImpl") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Replace with JAX-WS Web Service Implementation." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.wsee.async.AsyncPreCallContext") .at(TypeReferenceLocation.TYPE) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Asynchronous Web Service Client" ).with( Link.to( "Weblogic Asynchronous Execution Documentation" ,"http://docs.oracle.com/cd/E15051_01/wls/docs103/webserv_adv_rpc/asynch.html").to( "CXF Asynchronous Webservice Client Example" ,"http://singztechmusings.in/consuming-web-services-in-cxf-non-blocking-asynchronous-invocation-model/") ) .withEffort( 8
                    ) )
                    .endIteration()
                    )
                    
                    .addRule()
                    .when(
                    JavaClass.references("weblogic.wsee.connection.transport.http.HttpTransportInfo.setUsername\\(.+\\)") .at(TypeReferenceLocation.METHOD) ) .perform(
                    Iteration.over().perform(
                    Hint.withText("").with(Link.to("JAX-WS Proxy Password Example", "http://java-x.blogspot.com/2009/03/invoking-web-services-through-proxy.html")))
                    .endIteration()
                    )
                    
                    
                    .addRule()
                    .when(
                    JavaClass.references("weblogic.wsee.async.AsyncPreCallContext") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Replace with CXF Asynchronous Client." ).withEffort( 8 )
                    )
                    .endIteration()
                    )

                    //java-gate Link.to( "JAX-WS Proxy Password Example" ,"http://java-x.blogspot.com/2009/03/invoking-web-services-through-proxy.html") 
                    .addRule()
                    .when(
                    JavaClass.references("weblogic.common.T3StartupDef") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Weblogic Startup Service" ).with( Link.to( "EJB3.1 Singleton Bean" ,"http://docs.oracle.com/javaee/6/api/javax/ejb/Singleton.html").to( "EJB3.1 Startup Bean" ,"http://docs.oracle.com/javaee/6/api/javax/ejb/Startup.html") ) .withEffort( 4
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("weblogic.common.T3StartupDef") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Replace with EJB 3.1 @Singleton / @Startup annotations." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.common.T3ServicesDef") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Weblogic Startup Service" ).with( Link.to( "EJB3.1 Startup Bean" ,"http://docs.oracle.com/javaee/6/api/javax/ejb/Startup.html").to( "EJB3.1 Singleton Bean" ,"http://docs.oracle.com/javaee/6/api/javax/ejb/Singleton.html") ) .withEffort( 8
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("weblogic.common.T3ServicesDef") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "In JBoss 5, replace with an MBean." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.common.T3ServicesDef") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "In JBoss 6, replace with an @Singleton EJB." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.time.common.Triggerable$") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Weblogic Scheduled Job" ).with( Link.to( "JBoss EJB3.1 Scheduled Job" ,"http://jaitechwriteups.blogspot.com/2010/07/ejb31-timerservice-in-jboss-as-600m4.html").to( "JBoss EJB3.0 Timeout Job" ,"http://www.java2s.com/Code/Java/EJB3/EJBTutorialfromJBosstimer.htm") ) .withEffort( 0
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("weblogic.wsee.async.AsyncPreCallContext") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Replace with CXF Asynchronous Client." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.utils.StringUtils.*") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Replace with Apache Commons's StringUtils" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.apache.xml.+") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Replace weblogic.apache.xml with org.apache.xml (Xerces)" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.transaction.TransactionManager$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Replace with the JEE standard javax.transaction.TransactionManager" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.transaction.TransactionManager.resume") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Replace with the JEE standard javax.transaction.TransactionManager.resume(Transaction tx)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.transaction.TransactionManager.suspend\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Replace with the JEE standard javax.transaction.TransactionManager.suspend()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.transaction.TxHelper$") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Remove weblogic.transaction.TxHelper import" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.transaction.ClientTxHelper.getTransactionManager\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Look up the JEE javax.transaction.TransactionManager in JBoss using the javax.naming.InitialContext: java:TransactionManager" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.transaction.TxHelper.getTransactionManager\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Look up the JEE javax.transaction.TransactionManager in JBoss using the javax.naming.InitialContext: java:TransactionManager" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.common.T3StartupDef$") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "T3StartupDef defines a startup service in Weblogic. The main method is executed when the server starts up, if it is registered in the Weblogic context.xml The equivalent in JBoss 5 is to use a JMX Management annotation: org.jboss.annotation.ejb.Management." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.logging.NonCatalogLogger$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "NonCatalogLogger is a logger for logging messages to the Weblogic log; replace this with log4j, commons logging, or slf4j, with an appropriate log level." ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.time.common.Triggerable$") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Replace weblogic.time.common.Triggerable with EJB3 @Timeout" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.time.common.Triggerable$") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Replace weblogic.time.common.Triggerable with org.jboss.varia.scheduler.Schedulable for JBoss 5" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.jdbc.vendor.oracle.OracleThinClob") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Weblogic-specific Code; replace with oracle.sql.CLOB" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("(weblogic.jdbc.vendor.oracle.OracleThinClob|oracle.sql.CLOB)\\.getCharacterOutputStream\\(\\)") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Weblogic-specific Code; replace with oracle.sql.CLOB.getCharacterOutputStream(1)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.common.T3ServicesDef") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "In JBoss 5, look up the MBean replacement for the T3ServicesDef." ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.common.T3ServicesDef") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "In JBoss 6, inject the @Singleton, @Startup EJB3." ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.wsee.context.WebServiceContext") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Replace with javax.xml.ws.WebServiceContext" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("weblogic.wsee.context.ContextNotFoundException") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Weblogic specific; remove." ).withEffort( 0 )
                    )
                    .endIteration()
                    );


        return configuration;
    }
    // @formatter:on
}
