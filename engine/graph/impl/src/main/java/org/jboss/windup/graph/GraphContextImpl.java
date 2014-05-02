package org.jboss.windup.graph;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jboss.windup.graph.model.meta.ApplicationReferenceModel;
import org.jboss.windup.graph.model.meta.DatasourceMetaModel;
import org.jboss.windup.graph.model.meta.JBossModuleMetaModel;
import org.jboss.windup.graph.model.meta.JMSMetaModel;
import org.jboss.windup.graph.model.meta.JarManifestModel;
import org.jboss.windup.graph.model.meta.MailserverMetaModel;
import org.jboss.windup.graph.model.meta.PropertiesMetaModel;
import org.jboss.windup.graph.model.meta.SpecificationVersionMetaModel;
import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacetModel;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacetModel;
import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacetModel;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacetModel;
import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacetModel;
import org.jboss.windup.graph.model.meta.report.SourceReportModel;
import org.jboss.windup.graph.model.meta.xml.DoctypeMetaModel;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacetModel;
import org.jboss.windup.graph.model.meta.xml.HibernateConfigurationFacetModel;
import org.jboss.windup.graph.model.meta.xml.MavenFacetModel;
import org.jboss.windup.graph.model.meta.xml.NamespaceMetaModel;
import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacetModel;
import org.jboss.windup.graph.model.meta.xml.WebConfigurationFacetModel;
import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.jboss.windup.graph.model.resource.EarArchiveModel;
import org.jboss.windup.graph.model.resource.ImplementedInterfaceModel;
import org.jboss.windup.graph.model.resource.JarArchiveModel;
import org.jboss.windup.graph.model.resource.JavaClassModel;
import org.jboss.windup.graph.model.resource.JavaMethodModel;
import org.jboss.windup.graph.model.resource.JavaParameterModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.model.resource.WarArchiveModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

import com.esotericsoftware.minlog.Log;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.AbstractModule;
import com.tinkerpop.frames.modules.TypeResolver;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;

public class GraphContextImpl implements GraphContext {

	private TitanGraph graph;
	private BatchGraph<TitanGraph> batch;
	private FramedGraph<TitanGraph> framed;
	
	public TitanGraph getGraph() {
		return graph;
	}
	
	public BatchGraph<TitanGraph> getBatch() {
		return batch;
	}
	
	public FramedGraph<TitanGraph> getFramed() {
		return framed;
	}
	
	public GraphContextImpl(File diskCache) {
		FileUtils.deleteQuietly(diskCache);
		
		File lucene = new File(diskCache, "graphsearch");
		File berkley = new File(diskCache, "graph");
		
		Configuration conf = new BaseConfiguration();
		conf.setProperty("storage.directory", berkley.getAbsolutePath());
		conf.setProperty("storage.backend", "berkeleyje");
		
		conf.setProperty("storage.index.search.backend", "lucene");
		conf.setProperty("storage.index.search.directory", lucene.getAbsolutePath());
		conf.setProperty("storage.index.search.client-only", "false");
		conf.setProperty("storage.index.search.local-mode", "true");
		
		final String INDEX_NAME = "search";
		
		graph = TitanFactory.open(conf);
		//graph.createKeyIndex("archiveEntry", Vertex.class);
		//graph.createKeyIndex("filePath", Vertex.class, new Parameter<String, String>("type", "UNIQUE"));
		//graph.createKeyIndex("type", Vertex.class);
		
		TitanKey namespaceURIKey = graph.makeKey("namespaceURI").dataType(String.class).
				indexed(Vertex.class).make();

		TitanKey schemaLocationKey = graph.makeKey("schemaLocation").dataType(String.class).
				indexed(Vertex.class).make();
		
		TitanKey publicIdKey = graph.makeKey("publicId").dataType(String.class).
				indexed(Vertex.class).make();
		
		TitanKey rootTagKey = graph.makeKey("rootTagName").dataType(String.class).
				indexed(Vertex.class).make();
		
		TitanKey systemIdKey = graph.makeKey("systemId").dataType(String.class).
				indexed(Vertex.class).make();
		
		TitanKey qualifiedNameKey = graph.makeKey("qualifiedName").dataType(String.class).
				indexed(Vertex.class).unique().make();
		
		TitanKey archiveEntryKey = graph.makeKey("archiveEntry").dataType(String.class).
				indexed("search", Vertex.class).make();
		
		TitanKey typeKey = graph.makeKey("type").dataType(String.class).
				indexed(Vertex.class).make();
		
		TitanKey filePath = graph.makeKey("filePath").dataType(String.class).
				indexed(Vertex.class).unique().make();
		
		TitanKey mavenIdentifier = graph.makeKey("mavenIdentifier").dataType(String.class).
				indexed(Vertex.class).unique().make();
		
		batch = new BatchGraph<TitanGraph>(graph, 1000L);
		
		final TypeResolver customTypeResolver = new TypeResolver()
        {
            
            @Override
            public Class<?>[] resolveTypes(Edge e, Class<?> defaultType)
            {
                return new Class[] { };
            }
            
            @Override
            public Class<?>[] resolveTypes(Vertex v, Class<?> defaultType)
            {
                List<Class<?>> classes = new LinkedList<>();
                if (defaultType != ResourceModel.class) {
                    ResourceModel rModel = framed.frame(v, ResourceModel.class);
                    for (ImplementedInterfaceModel implementedIFace : rModel.getAllInterfaces()) {
                        System.out.println("TYPERESOLVER: " + implementedIFace.getClassName());
                        try {
                            classes.add(Class.forName(implementedIFace.getClassName()));
                        } catch (Exception e) {
                            Log.warn("Failed to load class with name: " + implementedIFace.getClassName());
                        }
                    }
                }
                return classes.toArray(new Class[0]);
            }
        };
        final FrameInitializer frameInitializer = new FrameInitializer()
        {
            @Override
            public void initElement(Class<?> kind, FramedGraph<?> framedGraph, Element element)
            {
                if (kind != ImplementedInterfaceModel.class && element instanceof Vertex) {
                    Vertex v = (Vertex)element;
                    ResourceModel resourceModel = framedGraph.frame(v, ResourceModel.class);
                    
                    boolean alreadyExists = false;
                    for (ImplementedInterfaceModel ifaceModel : resourceModel.getAllInterfaces()) {
                        String iface = ifaceModel.getClassName();
                        if (iface.equals(kind.getCanonicalName())) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (!alreadyExists) {
                        ImplementedInterfaceModel ifaceModel = framedGraph.addVertex(null, ImplementedInterfaceModel.class);
                        ifaceModel.setClassName(kind.getCanonicalName());
                        System.out.println("1FINIT added: " + kind.getCanonicalName());
                        resourceModel.addImplementedInterface(ifaceModel);
                    } else {
                        System.out.println("NOT ADDED: " + kind.getCanonicalName());
                    }
                    
                        
                }
            }
        };
		
		FramedGraphFactory factory = new FramedGraphFactory(
		        new AbstractModule() {
		            @Override
		            protected void doConfigure(FramedGraphConfiguration config)
		            {
		                config.addTypeResolver(customTypeResolver);
		                config.addFrameInitializer(frameInitializer);
		            }
		        },
				new JavaHandlerModule(),
			    new TypedGraphModuleBuilder()
				.withClass(ApplicationReferenceModel.class)
			    .withClass(ArchiveResourceModel.class)
			    .withClass(ArchiveEntryResourceModel.class)
			    .withClass(EarArchiveModel.class)
			    .withClass(org.jboss.windup.graph.model.resource.FileResourceModel.class)
		        .withClass(JarArchiveModel.class)
			    .withClass(JavaClassModel.class)
			    .withClass(JavaMethodModel.class)
                .withClass(JavaParameterModel.class)
		        .withClass(org.jboss.windup.graph.model.resource.ResourceModel.class)
			    .withClass(WarArchiveModel.class)
			    .withClass(XmlResourceModel.class)
			    
			    .withClass(EjbEntityFacetModel.class)
			    .withClass(EjbSessionBeanFacetModel.class)
			    .withClass(HibernateEntityFacetModel.class)
			    .withClass(MessageDrivenBeanFacetModel.class)
			    .withClass(SpringBeanFacetModel.class)
			    
			    .withClass(MavenFacetModel.class)
			    .withClass(EjbConfigurationFacetModel.class)
			    .withClass(SpringConfigurationFacetModel.class)
			    .withClass(HibernateConfigurationFacetModel.class)
			    .withClass(NamespaceMetaModel.class)
			    .withClass(DoctypeMetaModel.class)
			    
			    .withClass(DatasourceMetaModel.class)
				.withClass(JBossModuleMetaModel.class)
				.withClass(JMSMetaModel.class)
				.withClass(MailserverMetaModel.class)
				.withClass(SpecificationVersionMetaModel.class)
			    .withClass(JarManifestModel.class)
			    .withClass(SourceReportModel.class)
                .withClass(WebConfigurationFacetModel.class)
                .withClass(PropertiesMetaModel.class)
			    .build(), 
			    new GremlinGroovyModule()
		);
		
		framed = factory.create(graph);
	}
	
}
