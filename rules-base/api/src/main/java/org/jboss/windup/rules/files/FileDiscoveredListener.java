package org.jboss.windup.rules.files;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface FileDiscoveredListener
{
    FileDiscoveredResult fileDiscovered(FileDiscoveredEvent event);

    void fileModelCreated(GraphContext context, FileModel fileModel);
}
