package org.jboss.windup.rules.apps.xml.service;

import com.syncleus.ferma.Traversable;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.xml.model.DoctypeMetaModel;

import java.util.Iterator;

/**
 * Adds findByPublicIdAndSystemId().
 */
public class DoctypeMetaService extends GraphService<DoctypeMetaModel> {
    public DoctypeMetaService(GraphContext context) {
        super(context, DoctypeMetaModel.class);
    }

    @SuppressWarnings("unchecked")
    public Iterator<DoctypeMetaModel> findByPublicIdAndSystemId(String publicId, String systemId) {
        Traversable<?, ?> query = getGraphContext().getFramed().traverse(g -> g.V());
        if (StringUtils.isNotBlank(publicId)) {
            query.traverse(g -> g.has(DoctypeMetaModel.PROPERTY_PUBLIC_ID, publicId));
        }
        if (StringUtils.isNotBlank(systemId)) {
            query.traverse(g -> g.has(DoctypeMetaModel.PROPERTY_SYSTEM_ID, systemId));
        }
        return (Iterator<DoctypeMetaModel>) query.toList(DoctypeMetaModel.class).iterator();
    }
}
