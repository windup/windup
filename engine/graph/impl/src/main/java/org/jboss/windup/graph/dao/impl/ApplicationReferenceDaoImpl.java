package org.jboss.windup.graph.dao.impl;

import org.jboss.windup.graph.dao.ApplicationReferenceDao;
import org.jboss.windup.graph.model.meta.ApplicationReference;

public class ApplicationReferenceDaoImpl extends BaseDaoImpl<ApplicationReference> implements ApplicationReferenceDao
{
    public ApplicationReferenceDaoImpl() {
        super(ApplicationReference.class);
    }
}
