package org.jboss.windup.rules.apps.ejb.model;

import org.jboss.windup.rules.apps.java.scan.model.JarArchiveModel;
import org.jboss.windup.rules.apps.ejb.model.meta.xml.EjbConfigurationFacetModel;

import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.ArchiveModelPointer;

@TypeValue("WarArchiveResource")
public interface WarArchiveModel extends JarArchiveModel {

	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource')")
	public Iterable<JarArchiveModel> getJars();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('xmlResourceFacet').as('xml').out('xmlFacet').has('type', 'EJBConfigurationFacet')")
	public Iterable<EjbConfigurationFacetModel> getEjbConfigurations();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('xmlResourceFacet').as('xml').out('xmlFacet').has('type', 'SpringConfigurationFacet')")
	public Iterable<EjbConfigurationFacetModel> getSpringConfigurations();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('javaClassFacet').out('javaFacet').has('type', 'EJBSessionBean')")
	public Iterable<EjbSessionBeanFacetModel> getEjbSessionBeans();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('javaClassFacet').out('javaFacet').has('type', 'MessageDrivenBeanFacet')")
	public Iterable<EjbSessionBeanFacetModel> getMessageDrivenBeans();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('javaClassFacet').out('javaFacet').has('type', 'EJBEntityFacet')")
	public Iterable<EjbEntityFacetModel> getEjbEntityBeans();
	
	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource').out('child').out('javaClassFacet').out('javaFacet').has('type', 'SpringBeanFacet')")
	public Iterable<SpringBeanFacetModel> getSpringBeans();
    
    
    public static final class Pointer extends ArchiveModelPointer {
        @Override
        public String getArchiveFileSuffix() {
            return ".war";
        }

        @Override
        public Class getModelClass() {
            return WarArchiveModel.class;
        }
    }
}
