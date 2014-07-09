package org.jboss.windup.rules.apps.java.service;

import org.jboss.windup.graph.dao.BaseDaoImpl;
import org.jboss.windup.rules.apps.java.model.JavaParameterModel;

public class JavaParameterServiceImpl extends BaseDaoImpl<JavaParameterModel> implements JavaParameterService
{

    public JavaParameterServiceImpl()
    {
        super(JavaParameterModel.class);
    }
}
