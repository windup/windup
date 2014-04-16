package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.JavaClass;

public interface JavaClassDao extends BaseDao<JavaClass> {

	public JavaClass createJavaClass(String qualifiedName);
	
	public Iterable<JavaClass> findByJavaPackage(String packageName);
	
	public Iterable<JavaClass> findByJavaVersion(JavaVersion version);

	public Iterable<JavaClass> getAllClassNotFound();
	
	public Iterable<JavaClass> getAllDuplicateClasses();
	
	public enum JavaVersion {
		JAVA_7(7, 0),
		JAVA_6(6, 0),
		JAVA_5(5, 0),
		JAVA_1_4(1, 4),
		JAVA_1_3(1, 3),
		JAVA_1_2(1, 2),
		JAVA_1_1(1, 1);
		
		final int major;
		final int minor;
		
		JavaVersion(int major, int minor) {
			this.major = major;
			this.minor = minor;
		}
		
		public int getMajor() {
			return major;
		}
		
		public int getMinor() {
			return minor;
		}
	}


}
