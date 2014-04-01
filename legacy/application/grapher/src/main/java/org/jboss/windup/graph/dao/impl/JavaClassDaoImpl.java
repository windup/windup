package org.jboss.windup.graph.dao.impl;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.facet.JavaClassFacet;

public class JavaClassDaoImpl extends BaseDaoImpl<JavaClassFacet> implements JavaClassDao {

	public JavaClassDaoImpl(GraphContext context) {
		super(context, JavaClassFacet.class);
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.windup.graph.dao.impl.ClassGraphDaoA#getJavaClass(java.lang.String)
	 */
	@Override
	public JavaClassFacet getJavaClass(String qualifiedName) {
		JavaClassFacet clz = getByUniqueProperty("qualifiedName", qualifiedName);
		
		if(clz == null) {
			clz = (JavaClassFacet) this.create(null);
			clz.setQualifiedName(qualifiedName);
		}
		
		return clz;
	}
	
}
