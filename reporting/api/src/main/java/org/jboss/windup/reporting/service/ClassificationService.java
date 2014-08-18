package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;

/**
 * Service to interact with {@link ClassificationModel} instances.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ClassificationService extends GraphService<ClassificationModel>
{
    protected ClassificationService(Class<ClassificationModel> type)
    {
        super(type);
    }
}
