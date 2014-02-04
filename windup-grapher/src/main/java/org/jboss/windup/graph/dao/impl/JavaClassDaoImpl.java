package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.JavaClass;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

public class JavaClassDaoImpl extends BaseDaoImpl<JavaClass> implements
		JavaClassDao {

	public JavaClassDaoImpl(GraphContext context) {
		super(context, JavaClass.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.windup.graph.dao.impl.ClassGraphDaoA#getJavaClass(java.lang
	 * .String)
	 */
	@Override
	public JavaClass getJavaClass(String qualifiedName) {
		JavaClass clz = getByUniqueProperty("qualifiedName", qualifiedName);

		if (clz == null) {
			clz = (JavaClass) this.create(null);
			clz.setQualifiedName(qualifiedName);
		}

		return clz;
	}

	@Override
	public Iterable<JavaClass> getAllClassNotFound() {
		
		//iterate through all vertices
		Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(context
				.getGraph().getVertices("type", typeValue))
				
				//check to see whether there is an edge coming in that links to the resource providing the java class model.
				.filter(new PipeFunction<Vertex, Boolean>() {
					public Boolean compute(Vertex argument) {
						if (argument.getEdges(Direction.IN, "javaClassFacet").iterator().hasNext()) {
							return false;
						}
						//allow it through if there are no edges coming in that provide the java class model.
						return true;
					}
				});
		return context.getFramed().frameVertices(pipeline, JavaClass.class);
	}
	
	@Override
	public Iterable<JavaClass> getAllDuplicateClasses() {
		//iterate through all vertices
		Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(context
				.getGraph().getVertices("type", typeValue))
				
				//check to see whether there is an edge coming in that links to the resource providing the java class model.
				.filter(new PipeFunction<Vertex, Boolean>() {
					public Boolean compute(Vertex argument) {
						Iterator<Edge> edges = argument.getEdges(Direction.IN, "javaClassFacet").iterator();
						if(edges.hasNext()) {
							edges.next();
							if(edges.hasNext()) {
								return true;
							}
						}
						//if there aren't two edges, return false.
						return false;
					}
				});
		return context.getFramed().frameVertices(pipeline, JavaClass.class);
	}


}
