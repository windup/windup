package org.jboss.windup.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.jboss.windup.graph.dao.ClassGraphDao;
import org.jboss.windup.graph.dao.impl.ClassGraphDaoImpl;
import org.jboss.windup.graph.model.meta.JBossModule;
import org.jboss.windup.graph.model.meta.Meta;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.XmlDocument;
import org.jboss.windup.graph.renderer.GraphExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;

public class TestGraph {

	private static final Logger LOG = LoggerFactory.getLogger(TestGraph.class);
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		final int MAX_NODES = 30;
		
		File lucene = new File(FileUtils.getTempDirectory(), "graphsearch");
		FileUtils.deleteQuietly(lucene);
		
		File berkley = new File(FileUtils.getTempDirectory(), "graph");
		FileUtils.deleteQuietly(berkley);
		
		Configuration conf = new BaseConfiguration();
		conf.setProperty("storage.directory", berkley.getAbsolutePath());
		conf.setProperty("storage.backend", "berkeleyje");
		conf.setProperty("storage.index.search.backend", "lucene");
		conf.setProperty("storage.index.search.directory", lucene.getAbsolutePath());

		TitanGraph graph = TitanFactory.open(conf);
		graph.createKeyIndex("qualifiedName", Vertex.class, new Parameter("type", "UNIQUE"));
		
		FramedGraphFactory factory = new FramedGraphFactory(
			    new TypedGraphModuleBuilder()
			    .withClass(JavaClass.class)
		        .withClass(org.jboss.windup.graph.model.resource.File.class)
			    .withClass(XmlDocument.class)
			    .withClass(Meta.class)
			    .withClass(JBossModule.class)
			    .build()
		);

		FramedGraph framedGraph = factory.create(graph); //Frame the graph.

		ClassGraphDao graphDao = new ClassGraphDaoImpl(framedGraph);

		for(int i=0, j=MAX_NODES; i<j; i++) {
			int random1 = RandomUtils.nextInt(j);
			String name1 = "org.jboss.windup.Test"+random1;
			
			int random2 = RandomUtils.nextInt(j);
			String name2 = "org.jboss.windup.Test"+random2;
			
			if(name1.equals(name2)) {
				continue;
			}
			
			JavaClass main = graphDao.getJavaClass(name1);
			JavaClass impt = graphDao.getJavaClass(name2);
			
			main.asVertex().setProperty("blacklist", true);
			main.addImport(impt);
		}
		
		for(int i=0, j=MAX_NODES; i<j; i++) {
			int random1 = RandomUtils.nextInt(j);
			String name1 = "org.jboss.windup.Test"+random1;
			
			int random2 = RandomUtils.nextInt(j);
			String name2 = "org.jboss.windup.Test"+random2;
		
			if(name1.equals(name2)) {
				continue;
			}
			JavaClass main = graphDao.getJavaClass(name1);
			JavaClass impt = graphDao.getJavaClass(name2);
		
			main.addExtends(impt);
		}
		graph.commit();
		
		GraphExporter renders = new GraphExporter(graph);
		File targetFolder = FileUtils.getTempDirectory();
		
		renders.renderVizjs(new File(targetFolder, "vizjs.html"));
		renders.renderSigma(new File(targetFolder, "sigma.html"));
		renders.renderDagreD3(new File(targetFolder, "dagred3.html"));
		
		/*
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			GraphvizWriter writer = new GraphvizWriter(graph);
			writer.writeGraph(baos);
			
			LOG.info("Graphviz Result: "+baos.toString());
			
		} catch (ScriptException e) {
			LOG.error("Error!", e);
		}
		*/
	}
}
