package org.jboss.windup.rules.files;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.resource.FileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface FileDiscoveredListener
{
    FileDiscoveredResult fileDiscovered(FileDiscoveredEvent event);

    void fileModelCreated(GraphRewrite event, EvaluationContext context, FileModel fileModel);
}
