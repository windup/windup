package org.jboss.windup.rules.apps.xml.service;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.xml.model.DoctypeMetaModel;

import com.tinkerpop.frames.FramedGraphQuery;

public class DoctypeMetaService extends GraphService<DoctypeMetaModel>
{
    public DoctypeMetaService(GraphContext context)
    {
        super(context, DoctypeMetaModel.class);
    }

    public Iterator<DoctypeMetaModel> findByPublicIdAndSystemId(String publicId, String systemId)
    {
        FramedGraphQuery query = getGraphContext().getFramed().query();
        if (StringUtils.isNotBlank(publicId))
        {
            query.has(DoctypeMetaModel.PROPERTY_PUBLIC_ID, publicId);
        }
        if (StringUtils.isNotBlank(systemId))
        {
            query.has(DoctypeMetaModel.PROPERTY_SYSTEM_ID, systemId);
        }
        return query.vertices(DoctypeMetaModel.class).iterator();
    }
}
