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

public class SonicESBConfig extends WindupRuleProvider
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
        
        classifications.add(new JavaClassification(getID(), "Sonic ESB Service", "com.sonicsw.xq.XQService", 0, Types.add(ClassCandidateType.INHERITANCE)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQService", "<![CDATA[\n" + 
            "                Sonic ESB services inherit from XQService.  In Camel, this can be achieved through the simple Java Bean Camel Component.\n" + 
            "\n" + 
            "                * [Camel Java Bean Component](http://camel.apache.org/bean.html)\n" + 
            "            ]]>", 4, Types.add(ClassCandidateType.INHERITANCE)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQServiceContext", "<![CDATA[\n" + 
            "                Sonic ESB services leverage the service(XQServiceContext context) method to implement business logic.  When messages are routed to the service, the service(XQServiceContext context) method is executed. \n" + 
            "\n" + 
            "                In Camel, this is achieved by routing messages to the Java Bean via the Camel Route's Bean Component syntax.\n" + 
            "\n" + 
            "                * [Camel Java Bean Component](http://camel.apache.org/bean.html)\n" + 
            "                * [Camel Binding Annotations](http://camel.apache.org/parameter-binding-annotations.html)\n" + 
            "\n" + 
            "                Camel's Java Bean Component can leverage annotations annotations on the method to specify how Camel Message body values are mapped to the method parameters.  Additionally, the @Handler annotation can be leveraged to setup the default Java Bean method.\n" + 
            "\n" + 
            "                **For example:**\n" + 
            "                ```java\n" + 
            "                public void service(XQServiceContext ctx) throws XQServiceException {\n" + 
            "                    ...\n" + 
            "                }\n" + 
            "                ```\n" + 
            "\n" + 
            "                **Should become:**\n" + 
            "\n" + 
            "                ```java\n" + 
            "                @Handler\n" + 
            "                public void service(@Header messageHeader, @Body messageBody, Exchange exchange) {\n" + 
            "                    ...\n" + 
            "                }\n" + 
            "                ```\n" + 
            "\n" + 
            "                * org.apache.camel.Body\n" + 
            "                * org.apache.camel.Header\n" + 
            "                * org.apache.camel.Exchange\n" + 
            "\n" + 
            "            ]]>", 0, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQInitContext", "<![CDATA[\n" + 
            "                The XQInitContext is used to receive initialization information for the service from Sonic ESB.\n" + 
            "\n" + 
            "                This is not neccessary for Camel.  The init() method that receives this object should be replaced with Spring Bean property injection.  For initialization beyond propery injection, leverage Spring's @PostConstruct annotation on this init() method.\n" + 
            "                \n" + 
            "                * [Spring @PostConstruct Documentation](http://docs.spring.io/spring/docs/2.5.x/reference/beans.html#beans-postconstruct-and-predestroy-annotations)\n" + 
            "\n" + 
            "                ```java\n" + 
            "                @PostConstruct\n" + 
            "                public void init() {\n" + 
            "                    //leverage injected properties\n" + 
            "                }\n" + 
            "                ```\n" + 
            "            ]]>", 1, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQInitContext.getParameter", "Migrate to Spring property injection.", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQParameters.getParameter", "Migrate to Spring property injection.", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQParameters", "Migrate to Spring property injection.", 0, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQParameterInfo", "Migrate to Spring property injection.", 0, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQMessage$", "Migrate to org.apache.camel.Message", 0, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQMessage.getHeaderValue", "Migrate to org.apache.camel.Message.getHeader(String name)", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQMessage.setHeaderValue", "Migrate to org.apache.camel.Message.setHeader(String name, Object value)", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQMessage.getHeaderNames", "Migrate to org.apache.camel.Message.getHeaders()", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQPart", "Migrate XQPart to an attachment on the org.apache.camel.Message", 0, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQMessage.getPartCount", "Migrate to org.apache.camel.Message.getAttachments().size()", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQMessage.getPart\\(", "Migrate to org.apache.camel.Message.getAttachment(String id)", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQLog", "Migrate to SLF4J.", 1, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQServiceException", "<![CDATA[\n" + 
            "                Create a custom ServiceException class, extending Exception.  The documentation below explains exception handling in Camel.\n" + 
            "\n" + 
            "                * [Camel Exception Handling](http://camel.apache.org/exception-clause.html)\n" + 
            "            ]]>", 1, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQMessage.getCorrelationId", "<![CDATA[\n" + 
            "                Correlation is handled several ways in Camel.  Read the article below.\n" + 
            "\n" + 
            "                * [Camel Exception Handling](http://camel.apache.org/correlation-identifier.html)\n" + 
            "            ]]>", 1, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQAddressFactory.createEndpointAddress", "<![CDATA[\n" + 
            "                This indicates that the Sonic ESB Service is routing messages to a [1...N] endpoints.  To achieve this in Camel, take the business logic in the service, and populate a header property with an array of target enpoints.\n" + 
            "\n" + 
            "                Next, create a Recipient List processor to route the message to N endpoints.\n" + 
            "\n" + 
            "                * [Camel Recipient List](http://camel.apache.org/recipientlist-annotation.html)\n" + 
            "            ]]>", 3, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQServiceContext.addOutgoing", "<![CDATA[\n" + 
            "                Sonic ESB uses the addOutgoing method to set the outgoing message. \n" + 
            "\n" + 
            "                This is achieved in Camel by either modifying the @Body parameter in the Java Bean Component method, or literally setting a new message to the Exchange.\n" + 
            "\n" + 
            "                * [Camel Setting Response](http://camel.apache.org/using-getin-or-getout-methods-on-exchange.html)\n" + 
            "            ]]>", 1, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQEnvelope", "Migrate to org.apache.camel.Exchange", 0, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "com.sonicsw.xq.XQEnvelope.getMessage", "Migrate to com.sonicsw.xq.XQEnvelope.getMessage.getIn()", 0, Types.add(ClassCandidateType.METHOD))); 
        
        Configuration configuration = ConfigurationBuilder.begin()
            .addRule().perform(new ASTEventEvaluatorsBufferOperation().add(classifications).add(hints));
        return configuration;
        
    }
}
