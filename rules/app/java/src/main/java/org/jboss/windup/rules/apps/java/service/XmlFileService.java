package org.jboss.windup.rules.apps.java.service;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.xml.XmlFileModel;

/**
 * Contains methods for creating, querying, and deleting {@link XmlFileModel}s.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class XmlFileService extends GraphService<XmlFileModel>
{
    public XmlFileService()
    {
        super(XmlFileModel.class);
    }

    @Inject
    public XmlFileService(GraphContext context)
    {
        super(context, XmlFileModel.class);
    }
}
