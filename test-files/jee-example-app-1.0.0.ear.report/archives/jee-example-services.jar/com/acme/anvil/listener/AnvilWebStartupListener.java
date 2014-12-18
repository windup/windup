package com.acme.anvil.listener;

import javax.management.ObjectName;
import com.acme.anvil.management.AnvilInvokeBeanImpl;
import javax.naming.NamingException;
import javax.naming.Context;
import weblogic.jndi.Environment;
import javax.management.MBeanServer;
import java.util.Iterator;
import java.util.Hashtable;
import weblogic.common.T3ServicesDef;
import weblogic.i18n.logging.NonCatalogLogger;
import weblogic.common.T3StartupDef;

public class AnvilWebStartupListener implements T3StartupDef{
    private static final String MBEAN_NAME="com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanT3StartupDef";
    private NonCatalogLogger log;
    private T3ServicesDef services;
    public AnvilWebStartupListener(){
        super();
        this.log=new NonCatalogLogger("AnvilWebStartupListener");
    }
    public void setServices(final T3ServicesDef services){
        this.services=services;
    }
    public String startup(final String name,final Hashtable ht){
        this.log.info("Starting Server Startup Class: "+name+" with properties: ");
        for(final Object key : ht.keySet()){
            this.log.info("Key["+key+"] = Value["+ht.get(key)+"]");
        }
        return "Completed Startup Class: "+name;
    }
    private MBeanServer getMBeanServer() throws NamingException{
        final Environment env=new Environment();
        env.setProviderUrl("t3://weblogicServer:7001");
        env.setSecurityPrincipal("fred");
        env.setSecurityCredentials((Object)"seafood");
        final Context context=env.getContext();
        final MBeanServer server=(MBeanServer)context.lookup("java:comp/jmx/runtime");
        return server;
    }
    private void registerMBean(){
        this.log.info("Registering MBeans.");
        try{
            final MBeanServer server=this.getMBeanServer();
            server.registerMBean(new AnvilInvokeBeanImpl(),new ObjectName("com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanT3StartupDef"));
            this.log.info("Registered MBean[com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanT3StartupDef]");
        }
        catch(Exception e){
            this.log.error("Exception while registering MBean[com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanT3StartupDef]");
        }
    }
}
