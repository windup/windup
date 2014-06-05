package org.jboss.windup.graph.dao.impl;

import org.jboss.windup.graph.dao.JavaParameterDao;
import org.jboss.windup.graph.model.resource.JavaParameterModel;

public class JavaParameterDaoImpl extends BaseDaoImpl<JavaParameterModel> implements JavaParameterDao
{

    public JavaParameterDaoImpl()
    {
        super(JavaParameterModel.class);
    }
}
