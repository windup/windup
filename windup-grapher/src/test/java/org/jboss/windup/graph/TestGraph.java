package org.jboss.windup.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.dao.impl.JavaClassDaoImpl;
import org.jboss.windup.graph.model.resource.facet.JavaClassFacet;
import org.jboss.windup.graph.renderer.GraphExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestGraph {

	private static final Logger LOG = LoggerFactory.getLogger(TestGraph.class);
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		final int MAX_NODES = 1000;

		GraphContext context = new GraphContext(FileUtils.getTempDirectory());
		JavaClassDao graphDao = new JavaClassDaoImpl(context);

		for(int i=0, j=MAX_NODES; i<j; i++) {
			int random1 = RandomUtils.nextInt(j);
			String name1 = "org.jboss.windup.Test"+random1;
			
			int random2 = RandomUtils.nextInt(j);
			String name2 = "org.jboss.windup.Test"+random2;
			
			if(name1.equals(name2)) {
				continue;
			}
			
			JavaClassFacet main = graphDao.getJavaClass(name1);
			JavaClassFacet impt = graphDao.getJavaClass(name2);
			
			main.asVertex().setProperty("blacklist", true);
			main.addImport(impt);
			
			if(i % 1000 == 0) {
				context.getGraph().commit();
			}
			
			LOG.info("Pass: "+i); 
		}
		
		for(int i=0, j=MAX_NODES; i<j; i++) {
			int random1 = RandomUtils.nextInt(j);
			String name1 = "org.jboss.windup.Test"+random1;
			
			int random2 = RandomUtils.nextInt(j);
			String name2 = "org.jboss.windup.Test"+random2;
		
			if(name1.equals(name2)) {
				continue;
			}
			JavaClassFacet main = graphDao.getJavaClass(name1);
			JavaClassFacet impt = graphDao.getJavaClass(name2);
		
			if(i % 1000 == 0) {
				context.getGraph().commit();
			}
			
			main.setExtends(impt);
			LOG.info("Pass: "+i); 
		}
		
		GraphExporter renders = new GraphExporter(context.getGraph());
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
