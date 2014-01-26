package org.jboss.windup.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.jboss.windup.graph.dao.ClassGraphDao;
import org.jboss.windup.graph.model.FileResource;
import org.jboss.windup.graph.model.JavaClassResource;
import org.jboss.windup.graph.model.XmlResource;
import org.jboss.windup.graph.renderer.GraphExporter;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;

public class TestGraph {

	public static void main(String[] args) throws FileNotFoundException, IOException {
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
			    .withClass(JavaClassResource.class)
		        .withClass(FileResource.class)
			    .withClass(XmlResource.class)
			    .build()
		);

		FramedGraph framedGraph = factory.create(graph); //Frame the graph.

		ClassGraphDao graphDao = new ClassGraphDao(framedGraph);

		for(long i=0; i<10; i++) {
			int random1 = RandomUtils.nextInt(5);
			String name1 = "org.jboss.windup.Test"+random1;
			
			int random2 = RandomUtils.nextInt(5);
			String name2 = "org.jboss.windup.Test"+random2;
			
			if(name1.equals(name2)) {
				continue;
			}
			
			JavaClassResource main = graphDao.getJavaClass(name1);
			JavaClassResource impt = graphDao.getJavaClass(name2);
			
			main.addImport(impt);
		}
		
		for(long i=0; i<20; i++) {
			int random1 = RandomUtils.nextInt(5);
			String name1 = "org.jboss.windup.Test"+random1;
			
			int random2 = RandomUtils.nextInt(5);
			String name2 = "org.jboss.windup.Test"+random2;
		
			if(name1.equals(name2)) {
				continue;
			}
			JavaClassResource main = graphDao.getJavaClass(name1);
			JavaClassResource impt = graphDao.getJavaClass(name2);
		
			main.addExtends(impt);
		}
		graph.commit();
		
		GraphExporter renders = new GraphExporter(graph);
		File targetFolder = FileUtils.getTempDirectory();
		
		renders.renderVizjs(new File(targetFolder, "vizjs.html"));
		renders.renderSigma(new File(targetFolder, "sigma.html"));
		
	}
}
