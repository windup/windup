package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class Config extends WindupRuleProvider
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

        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(JavaClass.references("edu.oswego.cs.dl.util.concurrent"))
                    .perform(Hint.withText("Upgrade to javax.util.concurrent in Java 5+").withEffort(0))

                    .addRule()
                    .when(JavaClass.references("edu.emory.mathcs.backport.java.util"))
                    .perform(Hint.withText("Upgrade to javax.util.concurrent in Java 5+").withEffort(0))

                    .addRule()
                    .when(JavaClass.references("java.lang.Class.classForName").at(TypeReferenceLocation.METHOD))
                    .perform(Hint.withText("Ensure class is available to JBoss").withEffort(1))

                    .addRule()
                    .when(JavaClass.references("oracle.sql.*").at(TypeReferenceLocation.TYPE))
                    .perform(Hint.withText("Oracle-specific SQL code").withEffort(1))

                    .addRule()
                    .when(JavaClass.references("org.osoa.sca.annotations.+").at(TypeReferenceLocation.IMPORT))
                    .perform(Hint.withText("Remove import").withEffort(0))

                    .addRule()
                    .when(JavaClass.references("org.osoa.sca.annotations.Property")
                                            .at(TypeReferenceLocation.TYPE))
                    .perform(Hint.withText("SCA Property Injection; replace with Spring Property Injection").withEffort(0))

                    .addRule()
                    .when(JavaClass.references("org.osoa.sca.annotations.Reference").at(
                                            TypeReferenceLocation.TYPE))
                    .perform(Hint.withText("SCA Bean Injection; replace with Spring Bean Injection")
                                            .withEffort(0))

                    .addRule()
                    .when(JavaClass.references("org.osoa.sca.annotations.Init").at(TypeReferenceLocation.TYPE))
                    .perform(Hint.withText("SCA Initialization Hook; Use the property: init-method='example' on the Spring Bean, where example is the initialization method")
                                 .withEffort(0)
                    )

                    .addRule()
                    .when(JavaClass.references("org.osoa.sca.annotations.Destroy").at(TypeReferenceLocation.TYPE))
                    .perform(Hint.withText("SCA Destroy Hook; Use the property: destroy-method='example' on the Spring Bean, where example is the destroy method")
                                  .withEffort(0))

                    .addRule()
                    .when(JavaClass.references("com.ibm.ctg.client.JavaGateway").at(TypeReferenceLocation.TYPE))
                    .perform(Hint.withText("IBM CICS Adapter").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("((javax.naming.InitialContext)|(javax.naming.Context)).lookup")
                                            .at(TypeReferenceLocation.METHOD))
                    .perform(Hint.withText("<![CDATA[\n"
                                                        +
                                                        "                Ensure that the JNDI Name does not need to change for JBoss\n"
                                                        +
                                                        "                \n"
                                                        +
                                                        "                *For Example:*\n"
                                                        +
                                                        "                \n"
                                                        +
                                                        "                ```java\n"
                                                        +
                                                        "                (ConnectionFactory)initialContext.lookup(\"weblogic.jms.ConnectionFactory\");\n"
                                                        +
                                                        "                ```\n"
                                                        +
                                                        "                \n"
                                                        +
                                                        "                *should become:*\n"
                                                        +
                                                        "                \n"
                                                        +
                                                        "                ```java\n"
                                                        +
                                                        "                (ConnectionFactory)initialContext.lookup(\"/ConnectionFactory\");\n"
                                                        +
                                                        "                ```\n" +
                                                        "                \n" +
                                                        "                \n" +
                                                        "                ]]>").withEffort(1)
                    )

                    .addRule()
                    .when(JavaClass.references("javax.naming.InitialContext\\(.+\\)").at(
                                            TypeReferenceLocation.CONSTRUCTOR_CALL))
                    .perform(Hint.withText("Ensure that the InitialContext connection properties do not need to change for JBoss").withEffort(1))

                    .addRule()
                    .when(JavaClass.references("javax.management.remote.JMXServiceURL\\(.+\\)").at(
                                            TypeReferenceLocation.CONSTRUCTOR_CALL))
                    .perform(Hint.withText("Ensure that the connection properties do not need to change for JBoss")
                                 .withEffort(0))

                    .addRule()
                    .when(JavaClass.references("javax.management.ObjectName\\(.+\\)").at(
                                            TypeReferenceLocation.CONSTRUCTOR_CALL))
                    .perform(Hint.withText("Ensure that the ObjectName exists in JBoss").withEffort(1))

                    .addRule()
                    .when(JavaClass.references("javax.management.remote.JMXConnectorFactory.connect\\(.+\\)").at(
                                            TypeReferenceLocation.METHOD))
                    .perform(Hint.withText("Ensure that the connection properties do not need to change for JBoss")
                                 .withEffort(0))

                    .addRule()
                    .when(JavaClass.references("java.sql.DriverManager").at(TypeReferenceLocation.METHOD))
                    .perform(Hint.withText("Move to a JCA Connector unless this class is used for batch processes, then refactor as necessary")
                                 .withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("java.sql.DriverManager$").at(TypeReferenceLocation.IMPORT))
                    .perform(Hint.withText("Migrate to JCA Connector").withEffort(0))

                    .addRule()
                    .when(JavaClass.references("amx_.+").at(TypeReferenceLocation.IMPORT))
                    .perform(Hint.withText("Tibco ActiveMatrix Stub; regenerate the SOAP Client for the class")
                                 .withEffort(0)
                    )

                    .addRule()
                    .when(JavaClass.references("com.tibco.matrix.java.annotations.WebParam$"))
                    .perform(Hint.withText("Tibco specific annotation; replace with javax.jws.WebParam")
                                            .withEffort(0))

                    .addRule()
                    .when(JavaClass.references("com.tibco.amf.platform.runtime.extension.exception.SOAPCode$")
                                            )
                    .perform(Hint.withText("Tibco specific annotation").withEffort(0))

                    .addRule()
                    .when(JavaClass.references("com.tibco.matrix.java.annotations.WebServiceInterface$"))
                    .perform(Hint.withText("Tibco specific annotation; replace with javax.jws.WebService")
                                 .withEffort(0))

                    .addRule()
                    .when(JavaClass.references("com.tibco.matrix.java.annotations.WebMethod$"))
                    .perform(Hint.withText("Tibco specific annotation; replace with javax.jws.WebMethod")
                                 .withEffort(0))

                    .addRule()
                    .when(JavaClass.references("com.tibco.matrix.java.annotations.WebFault$"))
                    .perform(Hint.withText("Tibco specific annotation; replace with javax.xml.ws.WebFault")
                                 .withEffort(0))

                    .addRule()
                    .when(JavaClass.references("org.mule.transformers.AbstractTransformer$"))
                    .perform(Hint.withText("Mule specific; replace with org.apache.camel.Converter annotation")
                                 .withEffort(0))

                    .addRule()
                    .when(JavaClass.references("org.mule.umo.UMOMessage.getPayload.+").at(
                                            TypeReferenceLocation.METHOD))
                    .perform(Hint.withText("Mule specific; replace with org.apache.camel.Message.getBody()")
                                 .withEffort(0));

        return configuration;
    }
    // @formatter:on
}
