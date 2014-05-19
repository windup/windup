package com.acme.anvil.service.jms;

import com.acme.anvil.vo.LogEvent;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.transaction.Transaction;
import org.apache.log4j.Logger;
import weblogic.transaction.ClientTransactionManager;
import weblogic.transaction.TransactionHelper;

@Stateless
public class LogEventPublisher {

	private static final Logger log = Logger.getLogger(LogEventPublisher.class);
	private static final String QUEUE_JNDI_NAME = "java:jboss/jms/queue/LogEventQueue";
    
    @Inject private JMSContext context;
    @Resource(mappedName = QUEUE_JNDI_NAME) Queue myQueue;    

    
	public void publishLogEvent(LogEvent logEvent) {
		// Get a reference to the transaction manager to suspend the current transaction incase of exception.
		ClientTransactionManager ctm = TransactionHelper.getTransactionHelper().getTransactionManager();
		Transaction saveTx = null;
		try {
			saveTx = ctm.forceSuspend(); // Forced

                /*
                Context ic = getContext();
                QueueSession session = getQueueSession(ic);
                Queue queue = getQueue(ic);
                QueueSender sender = session.createSender(queue);
                ObjectMessage logMsg = session.createObjectMessage(logEvent);
                */

            ObjectMessage logMsg = context.createObjectMessage(logEvent);
            context.createProducer().send( myQueue, logMsg );

		} finally {
			ctm.forceResume(saveTx);
		}
	}

}
