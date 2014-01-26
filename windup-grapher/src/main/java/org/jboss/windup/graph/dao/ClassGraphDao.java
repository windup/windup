package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.JavaClassResource;

import com.tinkerpop.frames.FramedGraph;

public class ClassGraphDao {

	private final FramedGraph<?> graph;
	
	public ClassGraphDao(FramedGraph graph) {
		this.graph = graph;
	}
	
	public JavaClassResource getJavaClass(String qualifiedName) {
		JavaClassResource clz = null;
		
		for(JavaClassResource found : graph.getVertices("qualifiedName", qualifiedName, JavaClassResource.class)) {
			clz = found;
			break;
		}
		
		if(clz == null) {
			clz = (JavaClassResource) graph.addVertex(qualifiedName, JavaClassResource.class);
			clz.setQualifiedName(qualifiedName);
		}
		
		return clz;
	}
	
}
