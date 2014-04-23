package org.jboss.windup.graph.model.meta;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.FileResource;
import org.jboss.windup.graph.model.resource.Resource;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("PropertiesMeta")
public interface PropertiesMeta extends Resource
{

    @Adjacency(label = "propertiesFacet", direction = Direction.IN)
    public Resource getResource();

    @Adjacency(label = "propertiesFacet", direction = Direction.IN)
    public void setResource(Resource resource);

    @JavaHandler
    public String getProperty(String property);

    @JavaHandler
    public void setProperty(String propertyName, String obj);

    @JavaHandler
    public Set<String> keySet();

    @JavaHandler
    public InputStream asInputStream() throws RuntimeException;

    @JavaHandler
    public File asFile() throws RuntimeException;

    abstract class Impl implements PropertiesMeta, JavaHandlerContext<Vertex>
    {

        @Override
        public InputStream asInputStream() throws RuntimeException
        {
            try
            {
                Resource underlyingResource = this.getResource();
                if (underlyingResource instanceof ArchiveEntryResource)
                {
                    ArchiveEntryResource resource = frame(underlyingResource.asVertex(), ArchiveEntryResource.class);
                    return resource.asInputStream();
                }
                else if (underlyingResource instanceof FileResource)
                {
                    FileResource resource = frame(underlyingResource.asVertex(), FileResource.class);
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
                Resource underlyingResource = this.getResource();
                if (underlyingResource instanceof ArchiveEntryResource)
                {
                    ArchiveEntryResource resource = frame(underlyingResource.asVertex(), ArchiveEntryResource.class);
                    return resource.asFile();
                }
                else if (underlyingResource instanceof FileResource)
                {
                    FileResource resource = frame(underlyingResource.asVertex(), FileResource.class);
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
