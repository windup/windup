package org.jboss.windup.rules.apps.java.scan.dao;

import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.model.JavaMethodModel;

public interface JavaMethodDao extends BaseDao<JavaMethodModel>
{

    public JavaMethodModel createJavaMethod(JavaClassModel clz, String javaMethod, JavaClassModel ... params);
}
