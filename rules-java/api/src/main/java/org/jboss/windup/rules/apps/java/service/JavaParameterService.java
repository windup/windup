package org.jboss.windup.rules.apps.java.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaParameterModel;

public class JavaParameterService extends GraphService<JavaParameterModel>
{
    public JavaParameterService(GraphContext context)
    {
        super(context, JavaParameterModel.class);
    }
}
