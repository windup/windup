package org.jboss.windup.configuration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.windup.config.parser.HandlerManager;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class ReadXMLConfiguration extends TestCase {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ReadXMLConfiguration.class);
	
	
	@Test
	public void testLoadingFile() throws Exception {
		Weld weld = new Weld();
    	WeldContainer container = weld.initialize();
    	
    	ClassLoader classloader = this.getClass().getClassLoader();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(classloader.getResourceAsStream("windup-xml-config.xml"));
		
		HandlerManager parser = container.instance().select(HandlerManager.class).get();
		Object o = parser.processElement(doc.getDocumentElement());
		
		LOG.info("Object: "+ReflectionToStringBuilder.toString(o));
		weld.shutdown();
	}
}
