package com.acme.anvil.listener;

import com.acme.anvil.management.AnvilInvokeBeanImpl;
import java.lang.management.ManagementFactory;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.NamingException;
import org.apache.log4j.Logger;

/**
 * Originally, this was a subclass of ApplicationLifecycleListener.
 * There's no equivalent to preStart() and preStop().
 * See also:
 *   https://docs.jboss.org/author/display/AS72/How+do+I+migrate+my+application+from+WebLogic+to+AS+7#HowdoImigratemyapplicationfromWebLogictoAS7-ReplaceWebLogicApplicationLifecycleListenerCode .
 *   http://blog.eisele.net/2010/12/seven-ways-to-get-things-started-java.html
 *   https://access.redhat.com/site/solutions/199863
 */
@javax.ejb.Singleton
@javax.ejb.Startup
public class AnvilStartupSingleton {

	private static Logger LOG = Logger.getLogger(AnvilStartupSingleton.class);
	private static final String MBEAN_NAME = "com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBean";
    
    @Resource(lookup = "java:app/AppName") private String appName;
    
    
    @PostConstruct
    void postStart() {
		LOG.info("After Start Application["+appName+"]");
		registerMBean();
    }
 
    @PreDestroy
    void postStop() {
		LOG.info("Before Stop Application["+appName+"]");
		unregisterMBean();
    }    
	
    
	private MBeanServer getMBeanServer() throws NamingException {
		//MBeanServer server = (MBeanServer) new InitialContext().lookup("java:comp/jmx/runtime");
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		return server;
	}
	
	private void registerMBean() {
		LOG.info("Registering MBeans.");
		
		try {
			MBeanServer server = getMBeanServer();
			server.registerMBean(new AnvilInvokeBeanImpl(), new ObjectName(MBEAN_NAME));
			LOG.info("Registered MBean["+MBEAN_NAME+"]");
		} catch (Exception e) {
			LOG.error("Exception while registering MBean["+MBEAN_NAME+"]");
		}
	}
	
	private void unregisterMBean() {
		LOG.info("Unregistering MBeans.");
		
		try {
			MBeanServer server = getMBeanServer();
			server.unregisterMBean(new ObjectName(MBEAN_NAME));
			LOG.info("Unregistered MBean["+MBEAN_NAME+"]");
		} catch (Exception e) {
			LOG.error("Exception while unregistering MBean["+MBEAN_NAME+"]");
		}
	}
    
}// class
