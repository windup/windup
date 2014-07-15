package org.jboss.windup.rules.apps.java.model;

import java.util.Set;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JarManifestMeta")
public interface JarManifestModel extends FileModel
{
    @Adjacency(label = "archive", direction = Direction.IN)
    public void setJarArchive(final JarArchiveModel archive);

    @Adjacency(label = "archive", direction = Direction.IN)
    public JarArchiveModel getJarArchive();

    @JavaHandler
    public String getProperty(String property);

    @JavaHandler
    public void setProperty(String propertyName, String obj);

    @JavaHandler
    public Set<String> keySet();
}
