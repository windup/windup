package org.jboss.windup.rules.apps.java.scan.model;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.scan.model.JarArchiveModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JarManifestMeta") 
public interface JarManifestModel extends ResourceModel
{

    @Adjacency(label = "manifestFacet", direction = Direction.IN)
    public ResourceModel getResource();

    @Adjacency(label = "manifestFacet", direction = Direction.IN)
    public void setResource(ResourceModel resource);

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

    @Override
    @JavaHandler
    public InputStream asInputStream() throws RuntimeException;

    @Override
    @JavaHandler
    public File asFile() throws RuntimeException;

    abstract class Impl implements JarManifestModel, JavaHandlerContext<Vertex>
    {

        @Override
        public InputStream asInputStream() throws RuntimeException
        {
            try
            {
                ResourceModel underlyingResource = this.getResource();
                if (underlyingResource instanceof ArchiveEntryResourceModel)
                {
                    ArchiveEntryResourceModel resource = frame(underlyingResource.asVertex(), ArchiveEntryResourceModel.class);
                    return resource.asInputStream();
                }
                else if (underlyingResource instanceof FileModel)
                {
                    FileModel resource = frame(underlyingResource.asVertex(), FileModel.class);
                    return resource.asInputStream();
                }

                return this.getResource().asInputStream();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Exception reading resource.", e);
            }
        }

        @Override
        public File asFile() throws RuntimeException
        {
            try
            {
                ResourceModel underlyingResource = this.getResource();
                if (underlyingResource instanceof ArchiveEntryResourceModel)
                {
                    ArchiveEntryResourceModel resource = frame(underlyingResource.asVertex(), ArchiveEntryResourceModel.class);
                    return resource.asFile();
                }
                else if (underlyingResource instanceof FileModel)
                {
                    FileModel resource = frame(underlyingResource.asVertex(), FileModel.class);
                    return resource.asFile();
                }
                return this.getResource().asFile();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Exception reading resource.", e);
            }
        }

        @Override
        public String getProperty(String property)
        {
            return this.it().getProperty(property);
        }

        @Override
        public void setProperty(String propertyName, String obj)
        {
            this.it().setProperty(propertyName, obj);
        }

        @Override
        public Set<String> keySet()
        {
            return this.it().getPropertyKeys();
        }

        @Override
        public String toString()
        {
            return "Impl [keySet()=" + keySet() + ", getClass()=" + getClass()
                        + ", hashCode()=" + hashCode() + ", toString()="
                        + super.toString() + "]";
        }
    }

}
