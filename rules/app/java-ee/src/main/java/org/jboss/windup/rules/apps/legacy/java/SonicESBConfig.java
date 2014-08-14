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

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQService") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Sonic ESB Service" ).with( Link.to( "XQService Javadoc" ,"http://documentation.progress.com/output/Sonic/8.0.0/Docs8.0/api/esb_api/com/sonicsw/xq/XQService.html") ) .withEffort( 0
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQService") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( null ).withEffort( 4 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQServiceContext") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( null ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQInitContext") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( null ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQInitContext.getParameter") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to Spring property injection." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQParameters.getParameter") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to Spring property injection." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQParameters") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to Spring property injection." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQParameterInfo") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to Spring property injection." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQMessage$") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to org.apache.camel.Message" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQMessage.getHeaderValue") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to org.apache.camel.Message.getHeader(String name)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQMessage.setHeaderValue") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to org.apache.camel.Message.setHeader(String name, Object value)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQMessage.getHeaderNames") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to org.apache.camel.Message.getHeaders()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQPart") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Migrate XQPart to an attachment on the org.apache.camel.Message" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQMessage.getPartCount") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to org.apache.camel.Message.getAttachments().size()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQMessage.getPart\\(") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to org.apache.camel.Message.getAttachment(String id)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQLog") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to SLF4J." ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQServiceException") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( null ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQMessage.getCorrelationId") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( null ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQAddressFactory.createEndpointAddress") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( null ).withEffort( 3 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQServiceContext.addOutgoing") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( null ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQEnvelope") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to org.apache.camel.Exchange" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.sonicsw.xq.XQEnvelope.getMessage") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to com.sonicsw.xq.XQEnvelope.getMessage.getIn()" ).withEffort( 0 )
                    )
                    .endIteration()
                    );

        return configuration;
    }
    // @formatter:on
}
