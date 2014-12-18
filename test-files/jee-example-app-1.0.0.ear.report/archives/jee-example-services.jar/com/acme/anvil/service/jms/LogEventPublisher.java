package com.acme.anvil.service.jms;

import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import java.util.Hashtable;
import javax.naming.InitialContext;
import java.util.Properties;
import javax.jms.ObjectMessage;
import javax.jms.QueueSender;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.naming.Context;
import weblogic.transaction.Transaction;
import weblogic.transaction.ClientTransactionManager;
import javax.naming.NamingException;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.Serializable;
import weblogic.transaction.TransactionHelper;
import com.acme.anvil.vo.LogEvent;
import org.apache.log4j.Logger;

public class LogEventPublisher{
    private static final Logger LOG;
    private static final String QUEUE_JNDI_NAME="jms/LogEventQueue";
    private static final String QUEUE_FACTORY_JNDI_NAME="jms/LogEventQueue";
    public static void publishLogEvent(final LogEvent log){
        final ClientTransactionManager ctm=TransactionHelper.getTransactionHelper().getTransactionManager();
        Transaction saveTx=null;
        try{
            saveTx=ctm.forceSuspend();
            try{
                final Context ic=getContext();
                final QueueSession session=getQueueSession(ic);
                final Queue queue=getQueue(ic);
                final QueueSender sender=session.createSender(queue);
                final ObjectMessage logMsg=session.createObjectMessage((Serializable)log);
                sender.send((Message)logMsg);
            }
            catch(JMSException e){
                LogEventPublisher.LOG.error((Object)"Exception sending message.",(Throwable)e);
            }
            catch(NamingException e2){
                LogEventPublisher.LOG.error((Object)"Exception looking up required resource.",(Throwable)e2);
            }
        }
        finally{
            ctm.forceResume(saveTx);
        }
    }
    private static Context getContext() throws NamingException{
        final Properties environment=new Properties();
        ((Hashtable<String,String>)environment).put("java.naming.factory.initial","weblogic.jndi.WLInitialContextFactory");
        ((Hashtable<String,String>)environment).put("java.naming.provider.url","t3://localhost:7001");
        final Context context=new InitialContext(environment);
        return context;
    }
    private static Queue getQueue(final Context context) throws NamingException{
        return (Queue)context.lookup("jms/LogEventQueue");
    }
    private static QueueSession getQueueSession(final Context context) throws JMSException,NamingException{
        final QueueConnectionFactory cf=(QueueConnectionFactory)context.lookup("jms/LogEventQueue");
        final QueueConnection connection=cf.createQueueConnection();
        return (QueueSession)connection.createSession(false,1);
    }
    static{
        LOG=Logger.getLogger((Class)LogEventPublisher.class);
    }
}
