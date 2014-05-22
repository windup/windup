package com.acme.anvil.service.jms;

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import weblogic.transaction.ClientTransactionManager;
import weblogic.transaction.Transaction;
import weblogic.transaction.TransactionHelper;

import com.acme.anvil.vo.LogEvent;

public class LogEventPublisher {

	private static final Logger LOG = Logger.getLogger(LogEventPublisher.class);
	private static final String QUEUE_JNDI_NAME = "jms/LogEventQueue";
	private static final String QUEUE_FACTORY_JNDI_NAME = "jms/LogEventQueue";

	public static void publishLogEvent(LogEvent log) {
		//get a reference to the transaction manager to suspend the current transaction incase of exception.
		ClientTransactionManager ctm = TransactionHelper.getTransactionHelper().getTransactionManager();
		Transaction saveTx = null;
		try {
			saveTx = (Transaction) ctm.forceSuspend(); // Forced

			try {
				Context ic = getContext();
				QueueSession session = getQueueSession(ic);
				Queue queue = getQueue(ic);
				QueueSender sender = session.createSender(queue);
				ObjectMessage logMsg = session.createObjectMessage(log);

				sender.send(logMsg);
			} catch (JMSException e) {
				LOG.error("Exception sending message.", e);
			} catch (NamingException e) {
				LOG.error("Exception looking up required resource.", e);
			}

		} finally {
			ctm.forceResume(saveTx);
		}
	}

	private static Context getContext() throws NamingException {
		Properties environment = new Properties();
		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"weblogic.jndi.WLInitialContextFactory");
		environment.put(Context.PROVIDER_URL, "t3://localhost:7001");
		Context context = new InitialContext(environment);

		return context;
	}

	private static Queue getQueue(Context context) throws NamingException {
		return (Queue) context.lookup(QUEUE_JNDI_NAME);
	}

	private static QueueSession getQueueSession(Context context) throws JMSException, NamingException {
		QueueConnectionFactory cf = (QueueConnectionFactory) context
				.lookup(QUEUE_FACTORY_JNDI_NAME);
		QueueConnection connection = cf.createQueueConnection();
		return (QueueSession) connection.createSession(false, 1);
	}
}
