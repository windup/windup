package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.JavaMethod;

public interface JavaMethodDao extends BaseDao<JavaMethod>
{

    public JavaMethod createJavaMethod(JavaClass clz, String javaMethod, JavaClass ... params);
}
