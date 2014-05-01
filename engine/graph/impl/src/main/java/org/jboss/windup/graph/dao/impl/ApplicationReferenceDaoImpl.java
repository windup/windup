package org.jboss.windup.graph.dao.impl;

import org.jboss.windup.graph.dao.ApplicationReferenceDao;
import org.jboss.windup.graph.model.meta.ApplicationReferenceModel;

public class ApplicationReferenceDaoImpl extends BaseDaoImpl<ApplicationReferenceModel> implements ApplicationReferenceDao
{
    public ApplicationReferenceDaoImpl() {
        super(ApplicationReferenceModel.class);
    }
}
