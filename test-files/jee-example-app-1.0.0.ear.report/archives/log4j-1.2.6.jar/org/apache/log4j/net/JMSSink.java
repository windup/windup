package org.apache.log4j.net;

import javax.jms.MessageConsumer;
import javax.jms.Connection;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.jms.JMSException;
import org.apache.log4j.spi.LoggingEvent;
import javax.jms.ObjectMessage;
import javax.jms.Message;
import javax.jms.TopicConnection;
import javax.jms.Topic;
import javax.naming.Context;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.Logger;
import javax.jms.MessageListener;

public class JMSSink implements MessageListener{
    static Logger logger;
    static /* synthetic */ Class class$org$apache$log4j$net$JMSSink;
    public static void main(final String[] array) throws Exception{
        if(array.length!=5){
            usage("Wrong number of arguments.");
        }
        final String s=array[0];
        final String s2=array[1];
        final String s3=array[2];
        final String s4=array[3];
        final String s5=array[4];
        if(s5.endsWith(".xml")){
            new DOMConfigurator();
            DOMConfigurator.configure(s5);
        }
        else{
            new PropertyConfigurator();
            PropertyConfigurator.configure(s5);
        }
        new JMSSink(s,s2,s3,s4);
        final BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Type \"exit\" to quit JMSSink.");
        while(!bufferedReader.readLine().equalsIgnoreCase("exit")){
        }
        System.out.println("Exiting. Kill the application if it does not exit due to daemon threads.");
    }
    public JMSSink(final String s,final String s2,final String s3,final String s4){
        super();
        try{
            final InitialContext initialContext=new InitialContext();
            final TopicConnection topicConnection=((TopicConnectionFactory)lookup(initialContext,s)).createTopicConnection(s3,s4);
            ((Connection)topicConnection).start();
            ((MessageConsumer)topicConnection.createTopicSession(false,1).createSubscriber((Topic)initialContext.lookup(s2))).setMessageListener((MessageListener)this);
        }
        catch(Exception t){
            JMSSink.logger.error("Could not read JMS message.",t);
        }
    }
    public void onMessage(final Message message){
        try{
            if(message instanceof ObjectMessage){
                final LoggingEvent event=(LoggingEvent)((ObjectMessage)message).getObject();
                Logger.getLogger(event.getLoggerName()).callAppenders(event);
            }
            else{
                JMSSink.logger.warn("Received message is of type "+message.getJMSType()+", was expecting ObjectMessage.");
            }
        }
        catch(JMSException t){
            JMSSink.logger.error("Exception thrown while processing incoming message.",(Throwable)t);
        }
    }
    protected static Object lookup(final Context context,final String s) throws NamingException{
        try{
            return context.lookup(s);
        }
        catch(NameNotFoundException ex){
            JMSSink.logger.error("Could not find name ["+s+"].");
            throw ex;
        }
    }
    static void usage(final String s){
        System.err.println(s);
        System.err.println("Usage: java "+((JMSSink.class$org$apache$log4j$net$JMSSink==null)?(JMSSink.class$org$apache$log4j$net$JMSSink=class$("org.apache.log4j.net.JMSSink")):JMSSink.class$org$apache$log4j$net$JMSSink).getName()+" TopicConnectionFactoryBindingName TopicBindingName username password configFile");
        System.exit(1);
    }
    static /* synthetic */ Class class$(final String s){
        try{
            return Class.forName(s);
        }
        catch(ClassNotFoundException ex){
            throw new NoClassDefFoundError(ex.getMessage());
        }
    }
    static{
        JMSSink.logger=Logger.getLogger((JMSSink.class$org$apache$log4j$net$JMSSink==null)?(JMSSink.class$org$apache$log4j$net$JMSSink=class$("org.apache.log4j.net.JMSSink")):JMSSink.class$org$apache$log4j$net$JMSSink);
    }
}
