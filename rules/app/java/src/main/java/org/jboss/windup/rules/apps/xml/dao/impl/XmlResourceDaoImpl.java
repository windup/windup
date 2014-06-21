package org.jboss.windup.rules.apps.xml.dao.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.windup.rules.apps.xml.dao.NamespaceDao;
import org.jboss.windup.rules.apps.xml.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.xml.NamespaceMetaModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

import com.google.common.collect.Iterables;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.graph.dao.impl.BaseDaoImpl;

@Singleton
public class XmlResourceDaoImpl extends BaseDaoImpl<XmlResourceModel> implements XmlResourceDao {

    @Inject
    private NamespaceDao namespaceDao;
    
	public XmlResourceDaoImpl() {
		super(XmlResourceModel.class);
	}

	public Iterable<XmlResourceModel> containsNamespaceURI(String namespaceURI) {
		
		List<Iterable<XmlResourceModel>> result = new LinkedList<Iterable<XmlResourceModel>>();
		for(NamespaceMetaModel resource : namespaceDao.findByURI(namespaceURI)) {
			result.add(resource.getXmlResources());
		}
		
		//now, check thether it is null.
		if(result == null || result.size() == 0) {
			return Collections.emptyList();
		}
		return Iterables.concat(result);
	}
	
	public Iterable<XmlResourceModel> findByRootTag(String rootTagName) {
		return getByProperty("rootTagName", rootTagName);
	}
	
	public boolean isXmlResource(ResourceModel resource) {
        return (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("xmlResourceFacet").iterator().hasNext();
    }
    
    public XmlResourceModel getXmlFromResource(ResourceModel resource) {
        Iterator<Vertex> v = (new GremlinPipeline<Vertex, Vertex>(resource.asVertex())).out("xmlResourceFacet").iterator();
        if(v.hasNext()) {
            return getContext().getFramed().frame(v.next(), XmlResourceModel.class);
        }
        
        return null;
    }
}
