package org.jboss.windup.graph.dao.impl;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.dao.JavaMethodDao;
import org.jboss.windup.graph.dao.JavaParameterDao;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.JavaMethod;
import org.jboss.windup.graph.model.resource.JavaParameter;

public class JavaMethodDaoImpl extends BaseDaoImpl<JavaMethod> implements JavaMethodDao
{
    @Inject
    private JavaParameterDao paramDao;
    
    public JavaMethodDaoImpl()
    {
        super(JavaMethod.class);
    }

    public synchronized JavaMethod createJavaMethod(JavaClass clz, String javaMethod, JavaClass... params)
    {
        for (JavaMethod method : clz.getMethod(javaMethod))
        {
            if (method.countParameters() != params.length)
            {
                continue;
            }
            if (methodParametersMatch(method, params))
            {
                return method;
            }
        }

        JavaMethod method = create();
        method.setMethodName(javaMethod);

        for (int i = 0, j = params.length; i < j; i++)
        {
            JavaParameter param = paramDao.create();
            param.setPosition(i);
            param.setJavaType(params[i]);
        }

        return method;
    }

    protected boolean methodParametersMatch(JavaMethod method, JavaClass... params)
    {
        for (int i = 0, j = params.length; i < j; i++)
        {
            JavaParameter param = method.getParameter(i);
            JavaClass paramVal = param.getJavaType();
            if (!StringUtils.equals(paramVal.getQualifiedName(), paramVal.getQualifiedName()))
            {
                return false;
            }
        }

        return true;
    }

}
