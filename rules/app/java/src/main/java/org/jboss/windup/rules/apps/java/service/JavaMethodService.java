package org.jboss.windup.rules.apps.java.service;

import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;

public interface JavaMethodService extends BaseDao<JavaMethodModel>
{

    public JavaMethodModel createJavaMethod(JavaClassModel clz, String javaMethod, JavaClassModel... params);
}
