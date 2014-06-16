package org.jboss.windup.rules.apps.java.scan.dao;

import org.jboss.windup.graph.dao.impl.BaseDaoImpl;
import org.jboss.windup.rules.apps.java.scan.model.JavaParameterModel;

public class JavaParameterDaoImpl extends BaseDaoImpl<JavaParameterModel> implements JavaParameterDao
{

    public JavaParameterDaoImpl()
    {
        super(JavaParameterModel.class);
    }
}
