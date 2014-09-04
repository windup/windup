package org.jboss.windup.rules.apps.java.service;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;

/**
 * Manages the creation, querying, and deletion of {@link JarManifestModel}s.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class JarManifestService extends GraphService<JarManifestModel>
{

    public JarManifestService()
    {
        super(JarManifestModel.class);
    }

    @Inject
    public JarManifestService(GraphContext context)
    {
        super(context, JarManifestModel.class);
    }
}
