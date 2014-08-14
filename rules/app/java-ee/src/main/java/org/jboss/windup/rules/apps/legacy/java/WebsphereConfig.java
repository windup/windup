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

public class WebsphereConfig extends WindupRuleProvider
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
                    JavaClass.references("com.ibm.websphere.asynchbeans.Work") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Websphere Asyncronous Work").withEffort( 2
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("((com.ibm.websphere.startupservice.AppStartUpHome)|(com.ibm.websphere.startupservice.AppStartUp)|(com.ibm.websphere.startupservice.ModStartUpHome)|(com.ibm.websphere.startupservice.ModStartUp))$") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Websphere Startup Service" ).with( Link.to( "EJB3.1 Singleton Bean" ,"http://docs.oracle.com/javaee/6/api/javax/ejb/Singleton.html").to( "EJB3.1 Startup Bean" ,"http://docs.oracle.com/javaee/6/api/javax/ejb/Startup.html") ) .withEffort( 4
                    ) )
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.websphere.startupservice.AppStartUpHome") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Replace with EJB 3.1 @Singleton / @Startup annotations." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.websphere.asynchbeans.WorkManager") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to JBoss JCA Work Manager" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeConnection$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Connection" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeQueueConnection$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueConnection" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.ConnectionFactory" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeJNDIConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.ConnectionFactory" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeJNDIQueueConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.ConnectionFactory" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeQueueConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueConnectionFactory" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeConnectionMetaData$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.ConnectionMetaData" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeDestination$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Destination" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeJMSQueue$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Queue" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeJMSJNDIQueue$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( null ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeTemporaryQueue$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TemporaryQueue" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Message" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeBytesMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.BytesMessage" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeMapMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.MapMessage" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeObjectMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.ObjectMessage" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeStreamMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.StreamMessage" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeTextMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TextMessage" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeMessageConsumer$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.MessageConsumer" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeQueueReceiver$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueReceiver" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeMessageProducer$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.MessageProducer" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeQueueSender$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueSender" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeQueueBrowser$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueBrowser" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeSession$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Session" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mqe.jms.MQeQueueSession$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueSession" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQQueueConnectionFactory") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQXAQueueConnectionFactory") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XAQueueConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQTopicConnectionFactory") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TopicConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQXATopicConnectionFactory") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XATopicConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQXAConnectionFactory") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XAConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQDestination") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Destination" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQQueue") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Queue" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQTemporaryQueue") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TemporaryQueue" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQTopic") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Topic" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQTemporaryTopic") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TemporaryTopic" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQConnection") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Connection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQQueueConnection") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueConnection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQXAQueueConnection") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XAQueueConnection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQTopicConnection") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TopicConnection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQXATopicConnection") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XATopicConnection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQXAConnection") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XAConnection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQConnectionMetaData") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.ConnectionMetaData" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQMessageConsumer") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.MessageConsumer" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQQueueReceiver") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueReceiver" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQTopicSubscriber") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TopicSubscriber" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQMessageProducer") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.MessageProducer" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQQueueSender") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueSender" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQTopicPublisher") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TopicPublisher" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQQueueBrowser") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueBrowser" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQSession") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Session" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQQueueSession") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueSession" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQTopicSession") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TopicSession" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQXASession") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XASession" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQXAQueueSession") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XAQueueSession" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.mq.jms.MQXATopicSession") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XATopicSession" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.websphere.sib.api.jms.JmsTopicConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TopicConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.websphere.sib.api.jms.JmsTopic$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Topic" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.websphere.sib.api.jms.JmsQueueConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.websphere.sib.api.jms.JmsQueue$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Queue" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.websphere.sib.api.jms.JmsMsgProducer$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.MessageProducer" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.websphere.sib.api.jms.JmsMsgConsumer$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.MessageConsumer" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.websphere.sib.api.jms.JmsDestination$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Destination" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.websphere.sib.api.jms.JmsConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.ConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsConnection$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Connection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsConnectionMetaData$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.ConnectionMetaData" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsDestination$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Destination" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsMessageConsumer$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.MessageConsumer" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsMessageProducer$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.MessageProducer" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsQueue$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Queue" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsQueueBrowser$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueBrowser" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsQueueConnection$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueConnection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsQueueConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsQueueReceiver$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueReceiver" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsQueueSender$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueSender" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsQueueSession$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.QueueSession" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsSession$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Session" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsTemporaryQueue$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TemporaryQueue" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsTemporaryTopic$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TemporaryTopic" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsTopic$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Topic" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsTopicConnection$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TopicConnection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsTopicConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TopicConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsTopicPublisher$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TopicPublisher" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsTopicSession$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TopicSession" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsTopicSubscriber$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TopicSubscriber" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsXAConnection$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XAConnection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsXAConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XAConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsXAQueueConnection$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XAQueueConnection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsXAQueueConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XAQueueConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsXAQueueSession$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XAQueueSession" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsXASession$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XASession" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsXATopicConnection$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XATopicConnection" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsXATopicConnectionFactory$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XATopicConnectionFactory" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.msg.client.jms.JmsXATopicSession$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.XATopicSession" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.jms.JMSBytesMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.ByteMessage" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.jms.JMSMapMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.MapMessage" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.jms.JMSMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.Message" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.jms.JMSObjectMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.ObjectMessage" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.jms.JMSStreamMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.StreamMessage" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("com.ibm.jms.JMSTextMessage$") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform( Iteration.over().perform( Hint.withText( "Migrate to: javax.jms.TextMessage" ).withEffort( 0 )
                    )
                    .endIteration()
                    );

        return configuration;
   }
    // @formatter:on
}
