package com.acme.anvil.listener;

import javax.management.ObjectName;
import com.acme.anvil.management.AnvilInvokeBeanImpl;
import javax.naming.NamingException;
import javax.naming.Context;
import java.util.Hashtable;
import javax.naming.InitialContext;
import java.util.Properties;
import javax.management.MBeanServer;
import weblogic.application.ApplicationLifecycleEvent;
import org.apache.log4j.Logger;
import weblogic.application.ApplicationLifecycleListener;

public class AnvilWebLifecycleListener extends ApplicationLifecycleListener{
    private static Logger LOG;
    private static final String MBEAN_NAME="com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanApplicationLifecycleListener";
    public void preStart(final ApplicationLifecycleEvent evt){
        final String appName=evt.getApplicationContext().getApplicationName();
        AnvilWebLifecycleListener.LOG.info((Object)("Before Start Application["+appName+"]"));
    }
    public void postStart(final ApplicationLifecycleEvent evt){
        final String appName=evt.getApplicationContext().getApplicationName();
        AnvilWebLifecycleListener.LOG.info((Object)("After Start Application["+appName+"]"));
        this.registerMBean();
    }
    public void postStop(final ApplicationLifecycleEvent evt){
        final String appName=evt.getApplicationContext().getApplicationName();
        AnvilWebLifecycleListener.LOG.info((Object)("Before Stop Application["+appName+"]"));
        this.unregisterMBean();
    }
    public void preStop(final ApplicationLifecycleEvent evt){
        final String appName=evt.getApplicationContext().getApplicationName();
        AnvilWebLifecycleListener.LOG.info((Object)("After Stop Application["+appName+"]"));
    }
    private MBeanServer getMBeanServer() throws NamingException{
        final Properties environment=new Properties();
        ((Hashtable<String,String>)environment).put("java.naming.factory.initial","weblogic.jndi.WLInitialContextFactory");
        ((Hashtable<String,String>)environment).put("java.naming.provider.url","t3://localhost:7001");
        final Context context=new InitialContext(environment);
        final MBeanServer server=(MBeanServer)context.lookup("java:comp/jmx/runtime");
        return server;
    }
    private void registerMBean(){
        AnvilWebLifecycleListener.LOG.info((Object)"Registering MBeans.");
        try{
            final MBeanServer server=this.getMBeanServer();
            server.registerMBean(new AnvilInvokeBeanImpl(),new ObjectName("com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanApplicationLifecycleListener"));
            AnvilWebLifecycleListener.LOG.info((Object)"Registered MBean[com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanApplicationLifecycleListener]");
        }
        catch(Exception e){
            AnvilWebLifecycleListener.LOG.error((Object)"Exception while registering MBean[com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanApplicationLifecycleListener]");
        }
    }
    private void unregisterMBean(){
        AnvilWebLifecycleListener.LOG.info((Object)"Unregistering MBeans.");
        try{
            final MBeanServer server=this.getMBeanServer();
            server.unregisterMBean(new ObjectName("com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanApplicationLifecycleListener"));
            AnvilWebLifecycleListener.LOG.info((Object)"Unregistered MBean[com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanApplicationLifecycleListener]");
        }
        catch(Exception e){
            AnvilWebLifecycleListener.LOG.error((Object)"Exception while unregistering MBean[com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanApplicationLifecycleListener]");
        }
    }
    static{
        AnvilWebLifecycleListener.LOG=Logger.getLogger((Class)AnvilWebLifecycleListener.class);
    }
}
