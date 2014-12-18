package org.apache.log4j.net;

import javax.jms.Session;
import javax.jms.Connection;
import javax.jms.ObjectMessage;
import javax.jms.Message;
import java.io.Serializable;
import org.apache.log4j.spi.LoggingEvent;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.jms.Topic;
import javax.naming.Context;
import javax.jms.TopicConnectionFactory;
import java.util.Hashtable;
import javax.naming.InitialContext;
import java.util.Properties;
import org.apache.log4j.helpers.LogLog;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicConnection;
import org.apache.log4j.AppenderSkeleton;

public class JMSAppender extends AppenderSkeleton{
    String securityPrincipalName;
    String securityCredentials;
    String initialContextFactoryName;
    String urlPkgPrefixes;
    String providerURL;
    String topicBindingName;
    String tcfBindingName;
    String userName;
    String password;
    boolean locationInfo;
    TopicConnection topicConnection;
    TopicSession topicSession;
    TopicPublisher topicPublisher;
    public void setTopicConnectionFactoryBindingName(final String tcfBindingName){
        this.tcfBindingName=tcfBindingName;
    }
    public String getTopicConnectionFactoryBindingName(){
        return this.tcfBindingName;
    }
    public void setTopicBindingName(final String topicBindingName){
        this.topicBindingName=topicBindingName;
    }
    public String getTopicBindingName(){
        return this.topicBindingName;
    }
    public boolean getLocationInfo(){
        return this.locationInfo;
    }
    public void activateOptions(){
        try{
            LogLog.debug("Getting initial context.");
            InitialContext initialContext;
            if(this.initialContextFactoryName!=null){
                final Properties properties=new Properties();
                ((Hashtable<String,String>)properties).put("java.naming.factory.initial",this.initialContextFactoryName);
                if(this.providerURL!=null){
                    ((Hashtable<String,String>)properties).put("java.naming.provider.url",this.providerURL);
                }
                else{
                    LogLog.warn("You have set InitialContextFactoryName option but not the ProviderURL. This is likely to cause problems.");
                }
                if(this.urlPkgPrefixes!=null){
                    ((Hashtable<String,String>)properties).put("java.naming.factory.url.pkgs",this.urlPkgPrefixes);
                }
                if(this.securityPrincipalName!=null){
                    ((Hashtable<String,String>)properties).put("java.naming.security.principal",this.securityPrincipalName);
                    if(this.securityCredentials!=null){
                        ((Hashtable<String,String>)properties).put("java.naming.security.credentials",this.securityCredentials);
                    }
                    else{
                        LogLog.warn("You have set SecurityPrincipalName option but not the SecurityCredentials. This is likely to cause problems.");
                    }
                }
                initialContext=new InitialContext(properties);
            }
            else{
                initialContext=new InitialContext();
            }
            LogLog.debug("Looking up ["+this.tcfBindingName+"]");
            final TopicConnectionFactory topicConnectionFactory=(TopicConnectionFactory)this.lookup(initialContext,this.tcfBindingName);
            LogLog.debug("About to create TopicConnection.");
            if(this.userName!=null){
                this.topicConnection=topicConnectionFactory.createTopicConnection(this.userName,this.password);
            }
            else{
                this.topicConnection=topicConnectionFactory.createTopicConnection();
            }
            LogLog.debug("Creating TopicSession, non-transactional, in AUTO_ACKNOWLEDGE mode.");
            this.topicSession=this.topicConnection.createTopicSession(false,1);
            LogLog.debug("Looking up topic name ["+this.topicBindingName+"].");
            final Topic topic=(Topic)this.lookup(initialContext,this.topicBindingName);
            LogLog.debug("Creating TopicPublisher.");
            this.topicPublisher=this.topicSession.createPublisher(topic);
            LogLog.debug("Starting TopicConnection.");
            ((Connection)this.topicConnection).start();
            initialContext.close();
        }
        catch(Exception ex){
            super.errorHandler.error("Error while activating options for appender named ["+super.name+"].",ex,0);
        }
    }
    protected Object lookup(final Context context,final String s) throws NamingException{
        try{
            return context.lookup(s);
        }
        catch(NameNotFoundException ex){
            LogLog.error("Could not find name ["+s+"].");
            throw ex;
        }
    }
    protected boolean checkEntryConditions(){
        String s=null;
        if(this.topicConnection==null){
            s="No TopicConnection";
        }
        else if(this.topicSession==null){
            s="No TopicSession";
        }
        else if(this.topicPublisher==null){
            s="No TopicPublisher";
        }
        if(s!=null){
            super.errorHandler.error(s+" for JMSAppender named ["+super.name+"].");
            return false;
        }
        return true;
    }
    public synchronized void close(){
        if(super.closed){
            return;
        }
        LogLog.debug("Closing appender ["+super.name+"].");
        super.closed=true;
        try{
            if(this.topicSession!=null){
                ((Session)this.topicSession).close();
            }
            if(this.topicConnection!=null){
                ((Connection)this.topicConnection).close();
            }
        }
        catch(Exception t){
            LogLog.error("Error while closing JMSAppender ["+super.name+"].",t);
        }
        this.topicPublisher=null;
        this.topicSession=null;
        this.topicConnection=null;
    }
    public void append(final LoggingEvent object){
        if(!this.checkEntryConditions()){
            return;
        }
        try{
            final ObjectMessage objectMessage=((Session)this.topicSession).createObjectMessage();
            if(this.locationInfo){
                object.getLocationInformation();
            }
            objectMessage.setObject((Serializable)object);
            this.topicPublisher.publish((Message)objectMessage);
        }
        catch(Exception ex){
            super.errorHandler.error("Could not publish message in JMSAppender ["+super.name+"].",ex,0);
        }
    }
    public String getInitialContextFactoryName(){
        return this.initialContextFactoryName;
    }
    public void setInitialContextFactoryName(final String initialContextFactoryName){
        this.initialContextFactoryName=initialContextFactoryName;
    }
    public String getProviderURL(){
        return this.providerURL;
    }
    public void setProviderURL(final String providerURL){
        this.providerURL=providerURL;
    }
    String getURLPkgPrefixes(){
        return this.urlPkgPrefixes;
    }
    public void setURLPkgPrefixes(final String urlPkgPrefixes){
        this.urlPkgPrefixes=urlPkgPrefixes;
    }
    public String getSecurityCredentials(){
        return this.securityCredentials;
    }
    public void setSecurityCredentials(final String securityCredentials){
        this.securityCredentials=securityCredentials;
    }
    public String getSecurityPrincipalName(){
        return this.securityPrincipalName;
    }
    public void setSecurityPrincipalName(final String securityPrincipalName){
        this.securityPrincipalName=securityPrincipalName;
    }
    public String getUserName(){
        return this.userName;
    }
    public void setUserName(final String userName){
        this.userName=userName;
    }
    public String getPassword(){
        return this.password;
    }
    public void setPassword(final String password){
        this.password=password;
    }
    public void setLocationInfo(final boolean locationInfo){
        this.locationInfo=locationInfo;
    }
    public boolean requiresLayout(){
        return false;
    }
}
