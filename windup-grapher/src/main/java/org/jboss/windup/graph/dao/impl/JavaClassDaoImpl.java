package org.jboss.windup.graph.dao.impl;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.JavaClass;

public class JavaClassDaoImpl extends BaseDaoImpl<JavaClass> implements JavaClassDao {

	public JavaClassDaoImpl(GraphContext context) {
		super(context, JavaClass.class);
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.windup.graph.dao.impl.ClassGraphDaoA#getJavaClass(java.lang.String)
	 */
	@Override
	public JavaClass getJavaClass(String qualifiedName) {
		JavaClass clz = getByUniqueProperty("qualifiedName", qualifiedName);
		
		if(clz == null) {
			clz = (JavaClass) this.create(null);
			clz.setQualifiedName(qualifiedName);
		}
		
		return clz;
	}

	@Override
	public Iterable<JavaClass> getAllClassNotFound() {
		return this.getAll();
	}
	
}
