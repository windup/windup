package com.acme.anvil.listener;

import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import weblogic.common.T3ServicesDef;
import weblogic.common.T3StartupDef;
import weblogic.i18n.logging.NonCatalogLogger;
import weblogic.jndi.Environment;

import com.acme.anvil.management.AnvilInvokeBeanImpl;

/***
 * Prior to Weblogic 7, the T3StartupDef was a way of implementing startup listeners.
 *  See: http://docs.oracle.com/cd/E13222_01/wls/docs100/javadocs/weblogic/common/T3StartupDef.html
 *  See: http://docs.oracle.com/cd/E13222_01/wls/docs81/config_xml/StartupClass.html
 * @author bradsdavis
 *
 */
public class AnvilWebStartupListener implements T3StartupDef {

	private static final String MBEAN_NAME = "com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanT3StartupDef"; 
	private NonCatalogLogger log;
	
	public AnvilWebStartupListener() {
		//yes, this should be static final, but just for demo sake..
		log = new NonCatalogLogger("AnvilWebStartupListener");
	}
	
	private T3ServicesDef services;
	
	public void setServices(T3ServicesDef services) {
		this.services = services;
	}

	public String startup(String name, Hashtable ht) {
		log.info("Starting Server Startup Class: "+name+" with properties: ");
		
		for(Object key : ht.keySet()) {
			log.info("Key["+key+"] = Value["+ht.get(key)+"]");
		}
		
		return "Completed Startup Class: "+name;
	}

	
	private MBeanServer getMBeanServer() throws NamingException {
		//alternative way to create InitialContext reference.
		Environment env = new Environment();
		env.setProviderUrl("t3://weblogicServer:7001");
		env.setSecurityPrincipal("fred");
		env.setSecurityCredentials("seafood");
		Context context = env.getContext();
		
		//get reference to the MBean Server...
		MBeanServer server = (MBeanServer) context.lookup("java:comp/jmx/runtime");
		return server;
	}
	
	private void registerMBean() {
		log.info("Registering MBeans.");
		
		MBeanServer server;
		try {
			server = getMBeanServer();
			server.registerMBean(new AnvilInvokeBeanImpl(), new ObjectName(MBEAN_NAME));
			log.info("Registered MBean["+MBEAN_NAME+"]");
		} catch (Exception e) {
			log.error("Exception while registering MBean["+MBEAN_NAME+"]");
		}
	}
	
	
	
}
