package org.jboss.windup.rules.apps.java.service;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.dao.BaseDaoImpl;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.jboss.windup.rules.apps.java.model.JavaParameterModel;

public class JavaMethodServiceImpl extends BaseDaoImpl<JavaMethodModel> implements JavaMethodService
{
    @Inject
    private JavaParameterService paramDao;

    public JavaMethodServiceImpl()
    {
        super(JavaMethodModel.class);
    }

    public synchronized JavaMethodModel createJavaMethod(JavaClassModel clz, String javaMethod,
                JavaClassModel... params)
    {
        for (JavaMethodModel method : clz.getMethod(javaMethod))
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

        JavaMethodModel method = create();
        method.setMethodName(javaMethod);

        for (int i = 0, j = params.length; i < j; i++)
        {
            JavaParameterModel param = paramDao.create();
            param.setPosition(i);
            param.setJavaType(params[i]);
        }

        return method;
    }

    protected boolean methodParametersMatch(JavaMethodModel method, JavaClassModel... params)
    {
        for (int i = 0, j = params.length; i < j; i++)
        {
            JavaParameterModel param = method.getParameter(i);
            JavaClassModel paramVal = param.getJavaType();
            if (!StringUtils.equals(paramVal.getQualifiedName(), paramVal.getQualifiedName()))
            {
                return false;
            }
        }

        return true;
    }

}
