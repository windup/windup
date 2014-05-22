package com.acme.anvil.listener;

import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import weblogic.application.ApplicationLifecycleEvent;
import weblogic.application.ApplicationLifecycleListener;

import com.acme.anvil.management.AnvilInvokeBeanImpl;

/**
 * See more information on registering MBeans in Weblogic at:
 * 	http://docs.oracle.com/cd/E14571_01/web.1111/e13729/designapp.htm
 * 
 * This serves as an example on how to get Application Context information and register MBeans.
 * 
 * @author bradsdavis
 *
 */
public class AnvilWebLifecycleListener extends ApplicationLifecycleListener {

	private static Logger LOG = Logger.getLogger(AnvilWebLifecycleListener.class);
	private static final String MBEAN_NAME = "com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanApplicationLifecycleListener";
	
	@Override
	public void preStart(ApplicationLifecycleEvent evt) {
		String appName = evt.getApplicationContext().getApplicationName();
		LOG.info("Before Start Application["+appName+"]");
	}
	
	@Override
	public void postStart(ApplicationLifecycleEvent evt) {
		String appName = evt.getApplicationContext().getApplicationName();
		LOG.info("After Start Application["+appName+"]");
		registerMBean();
	}
	
	@Override
	public void postStop(ApplicationLifecycleEvent evt) {
		String appName = evt.getApplicationContext().getApplicationName();
		LOG.info("Before Stop Application["+appName+"]");
		unregisterMBean();
	}
	
	@Override
	public void preStop(ApplicationLifecycleEvent evt) {
		String appName = evt.getApplicationContext().getApplicationName();
		LOG.info("After Stop Application["+appName+"]");
	}
	
	private MBeanServer getMBeanServer() throws NamingException {
		Properties environment = new Properties();
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
		environment.put(Context.PROVIDER_URL, "t3://localhost:7001");
		Context context = new InitialContext(environment);
		
		//get reference to the MBean Server...
		MBeanServer server = (MBeanServer) context.lookup("java:comp/jmx/runtime");
		return server;
	}
	
	private void registerMBean() {
		LOG.info("Registering MBeans.");
		
		MBeanServer server;
		try {
			server = getMBeanServer();
			server.registerMBean(new AnvilInvokeBeanImpl(), new ObjectName(MBEAN_NAME));
			LOG.info("Registered MBean["+MBEAN_NAME+"]");
		} catch (Exception e) {
			LOG.error("Exception while registering MBean["+MBEAN_NAME+"]");
		}
	}
	
	private void unregisterMBean() {
		LOG.info("Unregistering MBeans.");
		
		MBeanServer server;
		try {
			server = getMBeanServer();
			server.unregisterMBean(new ObjectName(MBEAN_NAME));
			LOG.info("Unregistered MBean["+MBEAN_NAME+"]");
		} catch (Exception e) {
			LOG.error("Exception while unregistering MBean["+MBEAN_NAME+"]");
		}
	}
}
