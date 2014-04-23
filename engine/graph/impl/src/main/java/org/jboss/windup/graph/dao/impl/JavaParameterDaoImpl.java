package org.jboss.windup.graph.dao.impl;

import org.jboss.windup.graph.dao.JavaParameterDao;
import org.jboss.windup.graph.model.resource.JavaParameter;

public class JavaParameterDaoImpl extends BaseDaoImpl<JavaParameter> implements JavaParameterDao
{

    public JavaParameterDaoImpl()
    {
        super(JavaParameter.class);
    }
}
