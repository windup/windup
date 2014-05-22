package com.acme.anvil.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.logging.Logger;


/***
 * Prior to Weblogic 7, the T3StartupDef was a way of implementing startup listeners.
 *  See: http://docs.oracle.com/cd/E13222_01/wls/docs100/javadocs/weblogic/common/T3StartupDef.html
 *  See: http://docs.oracle.com/cd/E13222_01/wls/docs81/config_xml/StartupClass.html
 *  See: https://docs.jboss.org/author/display/WFLY8/Developer+Guide#DeveloperGuide-ModifycodetousetheNewJBossLoggingFramework
 */
//@WebServlet(loadOnStartup = 1)
@WebListener
public class AnvilWebListener implements ServletContextListener {

	private static final String MBEAN_NAME = "com.acme:Name=anvil,Type=com.acme.anvil.management.AnvilInvokeBeanT3StartupDef"; 
    
    private static final Logger log = Logger.getLogger(AnvilWebListener.class);

    
    @Override public void contextInitialized( ServletContextEvent sce ) {
		log.info("Initialized context, calling listener: "+ AnvilWebListener.class.getSimpleName() +" with properties: ");
        
		String name;
        Enumeration<String> names = sce.getServletContext().getAttributeNames();
        while( names.hasMoreElements() ) {
            name = names.nextElement();
			log.info("Attribute["+name+"] = Value["+sce.getServletContext().getAttribute(name)+"]");
		}
    }

    @Override public void contextDestroyed( ServletContextEvent sce ) {
        // jmxConnector.close();
    }
    
    
 
 	private MBeanServerConnection getMBeanServerConn() throws NamingException, MalformedURLException, IOException {

        //Get a connection to the WildFly 8 MBean server on localhost
        String host = "localhost";
        int port = 9990;  // management-web port
        String url = System.getProperty("jmx.service.url","service:jmx:http-remoting-jmx://" + host + ":" + port);
        JMXServiceURL serviceURL = new JMXServiceURL(url);

        // Provide credentials required by server for user authentication
        // See http://docs.oracle.com/cd/E19159-01/819-7758/gchjy/index.html
        Map env = new HashMap();
        env.put( JMXConnector.CREDENTIALS, new String[] {"fred", "seafood"} );
        
        JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, env);
        MBeanServerConnection conn = jmxConnector.getMBeanServerConnection();
 
        // Invoke on the WildFly 8 MBean server.
        //int count = conn.getMBeanCount();
        
        return conn;
    }

}// class
