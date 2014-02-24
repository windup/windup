package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacet;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;

import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("WarArchiveResource")
public interface WarArchive extends JarArchive {

	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource')")
	public Iterable<JarArchive> getJars();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('xmlResourceFacet').as('xml').out('xmlFacet').has('type', 'EJBConfigurationFacet')")
	public Iterable<EjbConfigurationFacet> getEjbConfigurations();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('xmlResourceFacet').as('xml').out('xmlFacet').has('type', 'SpringConfigurationFacet')")
	public Iterable<EjbConfigurationFacet> getSpringConfigurations();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('javaClassFacet').out('javaFacet').has('type', 'EJBSessionBean')")
	public Iterable<EjbSessionBeanFacet> getEjbSessionBeans();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('javaClassFacet').out('javaFacet').has('type', 'MessageDrivenBeanFacet')")
	public Iterable<EjbSessionBeanFacet> getMessageDrivenBeans();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('javaClassFacet').out('javaFacet').has('type', 'EJBEntityFacet')")
	public Iterable<EjbEntityFacet> getEjbEntityBeans();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('javaClassFacet').out('javaFacet').has('type', 'SpringBeanFacet')")
	public Iterable<SpringBeanFacet> getSpringBeans();
}
