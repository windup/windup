package org.jboss.windup.graph.dao.impl;

import org.jboss.windup.graph.dao.ClassGraphDao;
import org.jboss.windup.graph.model.resource.JavaClass;

import com.tinkerpop.frames.FramedGraph;

public class ClassGraphDaoImpl implements ClassGraphDao {

	private final FramedGraph<?> graph;
	
	public ClassGraphDaoImpl(FramedGraph graph) {
		this.graph = graph;
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.windup.graph.dao.impl.ClassGraphDaoA#getJavaClass(java.lang.String)
	 */
	@Override
	public JavaClass getJavaClass(String qualifiedName) {
		JavaClass clz = null;
		
		for(JavaClass found : graph.getVertices("qualifiedName", qualifiedName, JavaClass.class)) {
			clz = found;
			break;
		}
		
		if(clz == null) {
			clz = (JavaClass) graph.addVertex(qualifiedName, JavaClass.class);
			clz.setQualifiedName(qualifiedName);
		}
		
		return clz;
	}
	
}
