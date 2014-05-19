package com.acme.anvil.service.jms;


import java.text.SimpleDateFormat;

import javax.ejb.MessageDrivenBean;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.acme.anvil.vo.LogEvent;
import javax.jms.JMSConnectionFactoryDefinition;
import javax.jms.JMSDestinationDefinition;

import weblogic.ejb.GenericMessageDrivenBean;
import weblogic.ejbgen.MessageDriven;

// Not sure about this annotation, but my idea is that 
// having it at any "Java EE component class" will make the server create the queue.
@JMSDestinationDefinition(
    name = "java:jboss/jms/queue/LogEventQueue",
    destinationName="LogEventQueue", 
    description="Log Event Queue", 
    interfaceName = "javax.jms.Queue")
@JMSConnectionFactoryDefinition(name = "java:/AnotherConnectionFactory") 

@MessageDriven(
   ejbName = "LogEventSubscriber",
   destinationJndiName = "java:jboss/jms/queue/LogEventQueue",
   destinationType = "javax.jms.Topic",
   runAsPrincipalName = "anvil_user",
   runAs = "anvil_user"
)
public class LogEventSubscriber extends GenericMessageDrivenBean implements MessageDrivenBean, MessageListener {

	private static final Logger LOG = Logger.getLogger(LogEventSubscriber.class);
	private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm:ss z");
	
	public void onMessage(Message msg) {
		ObjectMessage om = (ObjectMessage)msg;
		Object obj;
		try {
			obj = om.getObject();
			
			if(obj instanceof LogEvent) {
				LogEvent event = (LogEvent)obj;
				LOG.info("Log Event ["+SDF.format(event.getDate())+"] : "+event.getMessage());
			}
		} catch (JMSException e) {
			LOG.error("Exception reading message.", e);
		}
	}
}
