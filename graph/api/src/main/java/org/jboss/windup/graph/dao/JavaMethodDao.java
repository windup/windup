package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.JavaClassModel;
import org.jboss.windup.graph.model.resource.JavaMethodModel;

public interface JavaMethodDao extends BaseDao<JavaMethodModel>
{

    public JavaMethodModel createJavaMethod(JavaClassModel clz, String javaMethod, JavaClassModel ... params);
}
