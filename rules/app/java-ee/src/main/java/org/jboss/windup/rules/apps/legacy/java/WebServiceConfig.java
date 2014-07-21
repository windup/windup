package org.jboss.windup.rules.apps.legacy.java;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.blacklist.BlackListRegex;
import org.jboss.windup.rules.apps.java.blacklist.JavaClassification;
import org.jboss.windup.rules.apps.java.blacklist.ASTEventEvaluatorsBufferOperation;
import org.jboss.windup.rules.apps.java.blacklist.Types;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
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

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
     
        List<JavaClassification> classifications = new ArrayList<JavaClassification>();
        List<BlackListRegex> hints = new ArrayList<BlackListRegex>();
        
        classifications.add(new JavaClassification(getID(), "JAX-RPC Service", "javax.xml.rpc.Service$", 0, Types.add(ClassCandidateType.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "Weblogic Web Service Implementation", "weblogic.wsee.jaxrpc.ServiceImpl", 6, Types.add(ClassCandidateType.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "Asynchronous Web Service Client", "weblogic.wsee.async.AsyncPreCallContext", 8, Types.add(ClassCandidateType.TYPE)));
        classifications.add(new JavaClassification(getID(), "Weblogic Startup Service", "weblogic.common.T3StartupDef", 4, Types.add(ClassCandidateType.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "Weblogic Startup Service", "weblogic.common.T3ServicesDef", 8, Types.add(ClassCandidateType.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "Weblogic Scheduled Job", "weblogic.time.common.Triggerable$", 0, Types.add(ClassCandidateType.INHERITANCE)));
        hints.add(new BlackListRegex(getID(), "weblogic.wsee.async.AsyncPreCallContext", "Replace with CXF Asynchronous Client.", 0));
        hints.add(new BlackListRegex(getID(), "weblogic.utils.StringUtils.*", "Replace with Apache Commons's StringUtils", 0));
        hints.add(new BlackListRegex(getID(), "weblogic.apache.xml.+", "Replace weblogic.apache.xml with org.apache.xml (Xerces)", 1));
        hints.add(new BlackListRegex(getID(), "weblogic.transaction.TransactionManager$", "Replace with the JEE standard javax.transaction.TransactionManager", 0));
        hints.add(new BlackListRegex(getID(), "weblogic.transaction.TransactionManager.resume", "Replace with the JEE standard javax.transaction.TransactionManager.resume(Transaction tx)", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "weblogic.transaction.TransactionManager.suspend\\(\\)", "Replace with the JEE standard javax.transaction.TransactionManager.suspend()", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "weblogic.transaction.TxHelper$", "Remove weblogic.transaction.TxHelper import", 0, Types.add(ClassCandidateType.IMPORT)));
        hints.add(new BlackListRegex(getID(), "weblogic.transaction.ClientTxHelper.getTransactionManager\\(\\)", "Look up the JEE javax.transaction.TransactionManager in JBoss using the javax.naming.InitialContext: java:TransactionManager", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "weblogic.transaction.TxHelper.getTransactionManager\\(\\)", "Look up the JEE javax.transaction.TransactionManager in JBoss using the javax.naming.InitialContext: java:TransactionManager", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "weblogic.common.T3StartupDef$", "T3StartupDef defines a startup service in Weblogic. The main method is executed when the server starts up, if it is registered in the Weblogic context.xml The equivalent in JBoss 5 is to use a JMX Management annotation: org.jboss.annotation.ejb.Management.", 0, Types.add(ClassCandidateType.INHERITANCE)));
        hints.add(new BlackListRegex(getID(), "weblogic.logging.NonCatalogLogger$", "NonCatalogLogger is a logger for logging messages to the Weblogic log; replace this with log4j, commons logging, or slf4j, with an appropriate log level.", 1));
        hints.add(new BlackListRegex(getID(), "weblogic.time.common.Triggerable$", "Replace weblogic.time.common.Triggerable with EJB3 @Timeout", 0, Types.add(ClassCandidateType.INHERITANCE)));
        hints.add(new BlackListRegex(getID(), "weblogic.time.common.Triggerable$", "Replace weblogic.time.common.Triggerable with org.jboss.varia.scheduler.Schedulable for JBoss 5", 0, Types.add(ClassCandidateType.INHERITANCE)));
        hints.add(new BlackListRegex(getID(), "weblogic.jdbc.vendor.oracle.OracleThinClob", "Weblogic-specific Code; replace with oracle.sql.CLOB", 0));
        hints.add(new BlackListRegex(getID(), "(weblogic.jdbc.vendor.oracle.OracleThinClob|oracle.sql.CLOB)\\.getCharacterOutputStream\\(\\)", "Weblogic-specific Code; replace with oracle.sql.CLOB.getCharacterOutputStream(1)", 0));
        hints.add(new BlackListRegex(getID(), "weblogic.common.T3ServicesDef", "In JBoss 5, look up the MBean replacement for the T3ServicesDef.", 1, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "weblogic.common.T3ServicesDef", "In JBoss 6, inject the @Singleton, @Startup EJB3.", 1, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "weblogic.wsee.context.WebServiceContext", "Replace with javax.xml.ws.WebServiceContext", 1));
        hints.add(new BlackListRegex(getID(), "weblogic.wsee.context.ContextNotFoundException", "Weblogic specific; remove.", 0)); 
        
        
        Configuration configuration = ConfigurationBuilder.begin()
            .addRule().perform(new ASTEventEvaluatorsBufferOperation().add(classifications).add(hints));
        return configuration;
        
    }
}
