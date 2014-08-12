package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
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
        /* TODO Change to use new Hints/classifications API
        
        List<JavaClassification> classifications = new ArrayList<JavaClassification>();
        List<BlackListRegex> hints = new ArrayList<BlackListRegex>();
        
        classifications.add(new JavaClassification(getID(), "Websphere Asyncronous Work", "com.ibm.websphere.asynchbeans.Work", 2, Types.add(TypeReferenceLocation.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "Websphere Startup Service", "((com.ibm.websphere.startupservice.AppStartUpHome)|(com.ibm.websphere.startupservice.AppStartUp)|(com.ibm.websphere.startupservice.ModStartUpHome)|(com.ibm.websphere.startupservice.ModStartUp))$", 4, Types.add(TypeReferenceLocation.INHERITANCE)));
        hints.add(new BlackListRegex(getID(), "com.ibm.websphere.asynchbeans.WorkManager", "Migrate to JBoss JCA Work Manager", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeConnection$", "Migrate to: javax.jms.Connection", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeQueueConnection$", "Migrate to: javax.jms.QueueConnection", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeConnectionFactory$", "Migrate to: javax.jms.ConnectionFactory", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeJNDIConnectionFactory$", "Migrate to: javax.jms.ConnectionFactory", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeJNDIQueueConnectionFactory$", "Migrate to: javax.jms.ConnectionFactory", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeQueueConnectionFactory$", "Migrate to: javax.jms.QueueConnectionFactory", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeConnectionMetaData$", "Migrate to: javax.jms.ConnectionMetaData", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeDestination$", "Migrate to: javax.jms.Destination", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeJMSQueue$", "Migrate to: javax.jms.Queue", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeJMSJNDIQueue$", null, 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeTemporaryQueue$", "Migrate to: javax.jms.TemporaryQueue", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeMessage$", "Migrate to: javax.jms.Message", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeBytesMessage$", "Migrate to: javax.jms.BytesMessage", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeMapMessage$", "Migrate to: javax.jms.MapMessage", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeObjectMessage$", "Migrate to: javax.jms.ObjectMessage", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeStreamMessage$", "Migrate to: javax.jms.StreamMessage", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeTextMessage$", "Migrate to: javax.jms.TextMessage", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeMessageConsumer$", "Migrate to: javax.jms.MessageConsumer", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeQueueReceiver$", "Migrate to: javax.jms.QueueReceiver", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeMessageProducer$", "Migrate to: javax.jms.MessageProducer", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeQueueSender$", "Migrate to: javax.jms.QueueSender", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeQueueBrowser$", "Migrate to: javax.jms.QueueBrowser", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeSession$", "Migrate to: javax.jms.Session", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mqe.jms.MQeQueueSession$", "Migrate to: javax.jms.QueueSession", 1));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQQueueConnectionFactory", "Migrate to: javax.jms.QueueConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQXAQueueConnectionFactory", "Migrate to: javax.jms.XAQueueConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQTopicConnectionFactory", "Migrate to: javax.jms.TopicConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQXATopicConnectionFactory", "Migrate to: javax.jms.XATopicConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQXAConnectionFactory", "Migrate to: javax.jms.XAConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQDestination", "Migrate to: javax.jms.Destination", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQQueue", "Migrate to: javax.jms.Queue", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQTemporaryQueue", "Migrate to: javax.jms.TemporaryQueue", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQTopic", "Migrate to: javax.jms.Topic", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQTemporaryTopic", "Migrate to: javax.jms.TemporaryTopic", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQConnection", "Migrate to: javax.jms.Connection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQQueueConnection", "Migrate to: javax.jms.QueueConnection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQXAQueueConnection", "Migrate to: javax.jms.XAQueueConnection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQTopicConnection", "Migrate to: javax.jms.TopicConnection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQXATopicConnection", "Migrate to: javax.jms.XATopicConnection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQXAConnection", "Migrate to: javax.jms.XAConnection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQConnectionMetaData", "Migrate to: javax.jms.ConnectionMetaData", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQMessageConsumer", "Migrate to: javax.jms.MessageConsumer", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQQueueReceiver", "Migrate to: javax.jms.QueueReceiver", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQTopicSubscriber", "Migrate to: javax.jms.TopicSubscriber", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQMessageProducer", "Migrate to: javax.jms.MessageProducer", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQQueueSender", "Migrate to: javax.jms.QueueSender", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQTopicPublisher", "Migrate to: javax.jms.TopicPublisher", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQQueueBrowser", "Migrate to: javax.jms.QueueBrowser", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQSession", "Migrate to: javax.jms.Session", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQQueueSession", "Migrate to: javax.jms.QueueSession", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQTopicSession", "Migrate to: javax.jms.TopicSession", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQXASession", "Migrate to: javax.jms.XASession", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQXAQueueSession", "Migrate to: javax.jms.XAQueueSession", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.mq.jms.MQXATopicSession", "Migrate to: javax.jms.XATopicSession", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.websphere.sib.api.jms.JmsTopicConnectionFactory$", "Migrate to: javax.jms.TopicConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.websphere.sib.api.jms.JmsTopic$", "Migrate to: javax.jms.Topic", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.websphere.sib.api.jms.JmsQueueConnectionFactory$", "Migrate to: javax.jms.QueueConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.websphere.sib.api.jms.JmsQueue$", "Migrate to: javax.jms.Queue", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.websphere.sib.api.jms.JmsMsgProducer$", "Migrate to: javax.jms.MessageProducer", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.websphere.sib.api.jms.JmsMsgConsumer$", "Migrate to: javax.jms.MessageConsumer", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.websphere.sib.api.jms.JmsDestination$", "Migrate to: javax.jms.Destination", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.websphere.sib.api.jms.JmsConnectionFactory$", "Migrate to: javax.jms.ConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsConnection$", "Migrate to: javax.jms.Connection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsConnectionMetaData$", "Migrate to: javax.jms.ConnectionMetaData", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsDestination$", "Migrate to: javax.jms.Destination", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsMessageConsumer$", "Migrate to: javax.jms.MessageConsumer", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsMessageProducer$", "Migrate to: javax.jms.MessageProducer", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsQueue$", "Migrate to: javax.jms.Queue", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsQueueBrowser$", "Migrate to: javax.jms.QueueBrowser", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsQueueConnection$", "Migrate to: javax.jms.QueueConnection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsQueueConnectionFactory$", "Migrate to: javax.jms.QueueConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsQueueReceiver$", "Migrate to: javax.jms.QueueReceiver", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsQueueSender$", "Migrate to: javax.jms.QueueSender", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsQueueSession$", "Migrate to: javax.jms.QueueSession", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsSession$", "Migrate to: javax.jms.Session", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsTemporaryQueue$", "Migrate to: javax.jms.TemporaryQueue", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsTemporaryTopic$", "Migrate to: javax.jms.TemporaryTopic", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsTopic$", "Migrate to: javax.jms.Topic", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsTopicConnection$", "Migrate to: javax.jms.TopicConnection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsTopicConnectionFactory$", "Migrate to: javax.jms.TopicConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsTopicPublisher$", "Migrate to: javax.jms.TopicPublisher", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsTopicSession$", "Migrate to: javax.jms.TopicSession", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsTopicSubscriber$", "Migrate to: javax.jms.TopicSubscriber", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsXAConnection$", "Migrate to: javax.jms.XAConnection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsXAConnectionFactory$", "Migrate to: javax.jms.XAConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsXAQueueConnection$", "Migrate to: javax.jms.XAQueueConnection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsXAQueueConnectionFactory$", "Migrate to: javax.jms.XAQueueConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsXAQueueSession$", "Migrate to: javax.jms.XAQueueSession", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsXASession$", "Migrate to: javax.jms.XASession", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsXATopicConnection$", "Migrate to: javax.jms.XATopicConnection", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsXATopicConnectionFactory$", "Migrate to: javax.jms.XATopicConnectionFactory", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.msg.client.jms.JmsXATopicSession$", "Migrate to: javax.jms.XATopicSession", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.jms.JMSBytesMessage$", "Migrate to: javax.jms.ByteMessage", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.jms.JMSMapMessage$", "Migrate to: javax.jms.MapMessage", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.jms.JMSMessage$", "Migrate to: javax.jms.Message", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.jms.JMSObjectMessage$", "Migrate to: javax.jms.ObjectMessage", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.jms.JMSStreamMessage$", "Migrate to: javax.jms.StreamMessage", 0));
        hints.add(new BlackListRegex(getID(), "com.ibm.jms.JMSTextMessage$", "Migrate to: javax.jms.TextMessage", 0)); 
        
        Configuration configuration = ConfigurationBuilder.begin()
            .addRule().perform(new JavaScanner().add(classifications).add(hints));
        return configuration;
        */
        return null;
   }
    // @formatter:on
}
