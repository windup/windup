package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
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
    public void enhanceMetadata(Context context)
    {
        super.enhanceMetadata(context);
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(JavaClass.references("com.ibm.websphere.asynchbeans.Work").at(TypeReferenceLocation.INHERITANCE))
                    .perform(Classification.as("Websphere Asyncronous Work").withEffort(2))
                    
                    .addRule()
                    .when(JavaClass.references("((com.ibm.websphere.startupservice.AppStartUpHome)|(com.ibm.websphere.startupservice.AppStartUp)|(com.ibm.websphere.startupservice.ModStartUpHome)|(com.ibm.websphere.startupservice.ModStartUp))$")
                                            .at(TypeReferenceLocation.INHERITANCE))
                    .perform(Classification.as("Websphere Startup Service")
                                                                    .with(Link.to("EJB3.1 Singleton Bean",
                                                                                "http://docs.oracle.com/javaee/6/api/javax/ejb/Singleton.html"))
                                                                                .with(Link.to("EJB3.1 Startup Bean",
                                                                                    "http://docs.oracle.com/javaee/6/api/javax/ejb/Startup.html"))
                                                                    .withEffort(4))
                                            
                    .addRule()
                    .when(JavaClass.references("com.ibm.websphere.startupservice.AppStartUpHome"))
                    .perform(Hint.withText("Replace with EJB 3.1 @Singleton / @Startup annotations.")
                                            .withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.websphere.asynchbeans.WorkManager"))
                    .perform(Hint.withText("Migrate to JBoss JCA Work Manager").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeConnection$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Connection").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeQueueConnection$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueConnection").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.ConnectionFactory").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeJNDIConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.ConnectionFactory").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeJNDIQueueConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.ConnectionFactory").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeQueueConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueConnectionFactory").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeConnectionMetaData$"))
                    .perform(Hint.withText("Migrate to: javax.jms.ConnectionMetaData").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeDestination$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Destination").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeJMSQueue$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Queue").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeJMSJNDIQueue$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Queue").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeTemporaryQueue$"))
                    .perform(Hint.withText("Migrate to: javax.jms.TemporaryQueue").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Message").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeBytesMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.BytesMessage").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeMapMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.MapMessage").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeObjectMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.ObjectMessage").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeStreamMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.StreamMessage").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeTextMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.TextMessage").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeMessageConsumer$"))
                    .perform(Hint.withText("Migrate to: javax.jms.MessageConsumer").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeQueueReceiver$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueReceiver").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeMessageProducer$"))
                    .perform(Hint.withText("Migrate to: javax.jms.MessageProducer").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeQueueSender$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueSender").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeQueueBrowser$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueBrowser").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeSession$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Session").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mqe.jms.MQeQueueSession$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueSession").withEffort(1))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQQueueConnectionFactory"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQXAQueueConnectionFactory"))
                    .perform(Hint.withText("Migrate to: javax.jms.XAQueueConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQTopicConnectionFactory"))
                    .perform(Hint.withText("Migrate to: javax.jms.TopicConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQXATopicConnectionFactory"))
                    .perform(Hint.withText("Migrate to: javax.jms.XATopicConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQXAConnectionFactory"))
                    .perform(Hint.withText("Migrate to: javax.jms.XAConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQDestination"))
                    .perform(Hint.withText("Migrate to: javax.jms.Destination").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQQueue"))
                    .perform(Hint.withText("Migrate to: javax.jms.Queue").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQTemporaryQueue"))
                    .perform(Hint.withText("Migrate to: javax.jms.TemporaryQueue").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQTopic"))
                    .perform(Hint.withText("Migrate to: javax.jms.Topic").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQTemporaryTopic"))
                    .perform(Hint.withText("Migrate to: javax.jms.TemporaryTopic").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQConnection"))
                    .perform(Hint.withText("Migrate to: javax.jms.Connection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQQueueConnection"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueConnection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQXAQueueConnection"))
                    .perform(Hint.withText("Migrate to: javax.jms.XAQueueConnection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQTopicConnection"))
                    .perform(Hint.withText("Migrate to: javax.jms.TopicConnection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQXATopicConnection"))
                    .perform(Hint.withText("Migrate to: javax.jms.XATopicConnection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQXAConnection"))
                    .perform(Hint.withText("Migrate to: javax.jms.XAConnection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQConnectionMetaData"))
                    .perform(Hint.withText("Migrate to: javax.jms.ConnectionMetaData").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQMessageConsumer"))
                    .perform(Hint.withText("Migrate to: javax.jms.MessageConsumer").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQQueueReceiver"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueReceiver").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQTopicSubscriber"))
                    .perform(Hint.withText("Migrate to: javax.jms.TopicSubscriber").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQMessageProducer"))
                    .perform(Hint.withText("Migrate to: javax.jms.MessageProducer").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQQueueSender"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueSender").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQTopicPublisher"))
                    .perform(Hint.withText("Migrate to: javax.jms.TopicPublisher").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQQueueBrowser"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueBrowser").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQSession"))
                    .perform(Hint.withText("Migrate to: javax.jms.Session").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQQueueSession"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueSession").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQTopicSession"))
                    .perform(Hint.withText("Migrate to: javax.jms.TopicSession").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQXASession"))
                    .perform(Hint.withText("Migrate to: javax.jms.XASession").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQXAQueueSession"))
                    .perform(Hint.withText("Migrate to: javax.jms.XAQueueSession").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.mq.jms.MQXATopicSession"))
                    .perform(Hint.withText("Migrate to: javax.jms.XATopicSession").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.websphere.sib.api.jms.JmsTopicConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.TopicConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.websphere.sib.api.jms.JmsTopic$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Topic").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.websphere.sib.api.jms.JmsQueueConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.websphere.sib.api.jms.JmsQueue$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Queue").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.websphere.sib.api.jms.JmsMsgProducer$"))
                    .perform(Hint.withText("Migrate to: javax.jms.MessageProducer").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.websphere.sib.api.jms.JmsMsgConsumer$"))
                    .perform(Hint.withText("Migrate to: javax.jms.MessageConsumer").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.websphere.sib.api.jms.JmsDestination$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Destination").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.websphere.sib.api.jms.JmsConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.ConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsConnection$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Connection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsConnectionMetaData$"))
                    .perform(Hint.withText("Migrate to: javax.jms.ConnectionMetaData").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsDestination$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Destination").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsMessageConsumer$"))
                    .perform(Hint.withText("Migrate to: javax.jms.MessageConsumer").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsMessageProducer$"))
                    .perform(Hint.withText("Migrate to: javax.jms.MessageProducer").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsQueue$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Queue").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsQueueBrowser$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueBrowser").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsQueueConnection$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueConnection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsQueueConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsQueueReceiver$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueReceiver").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsQueueSender$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueSender").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsQueueSession$"))
                    .perform(Hint.withText("Migrate to: javax.jms.QueueSession").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsSession$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Session").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsTemporaryQueue$"))
                    .perform(Hint.withText("Migrate to: javax.jms.TemporaryQueue").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsTemporaryTopic$"))
                    .perform(Hint.withText("Migrate to: javax.jms.TemporaryTopic").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsTopic$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Topic").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsTopicConnection$"))
                    .perform(Hint.withText("Migrate to: javax.jms.TopicConnection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsTopicConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.TopicConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsTopicPublisher$"))
                    .perform(Hint.withText("Migrate to: javax.jms.TopicPublisher").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsTopicSession$"))
                    .perform(Hint.withText("Migrate to: javax.jms.TopicSession").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsTopicSubscriber$"))
                    .perform(Hint.withText("Migrate to: javax.jms.TopicSubscriber").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsXAConnection$"))
                    .perform(Hint.withText("Migrate to: javax.jms.XAConnection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsXAConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.XAConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsXAQueueConnection$"))
                    .perform(Hint.withText("Migrate to: javax.jms.XAQueueConnection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsXAQueueConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.XAQueueConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsXAQueueSession$"))
                    .perform(Hint.withText("Migrate to: javax.jms.XAQueueSession").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsXASession$"))
                    .perform(Hint.withText("Migrate to: javax.jms.XASession").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsXATopicConnection$"))
                    .perform(Hint.withText("Migrate to: javax.jms.XATopicConnection").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsXATopicConnectionFactory$"))
                    .perform(Hint.withText("Migrate to: javax.jms.XATopicConnectionFactory").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.msg.client.jms.JmsXATopicSession$"))
                    .perform(Hint.withText("Migrate to: javax.jms.XATopicSession").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.jms.JMSBytesMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.ByteMessage").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.jms.JMSMapMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.MapMessage").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.jms.JMSMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.Message").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.jms.JMSObjectMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.ObjectMessage").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.jms.JMSStreamMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.StreamMessage").withEffort(0))
                                

                    .addRule()
                    .when(JavaClass.references("com.ibm.jms.JMSTextMessage$"))
                    .perform(Hint.withText("Migrate to: javax.jms.TextMessage").withEffort(0));

        return configuration;
    }
    // @formatter:on
}
