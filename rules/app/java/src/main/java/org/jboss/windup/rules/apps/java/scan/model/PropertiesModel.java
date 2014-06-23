package org.jboss.windup.rules.apps.java.scan.model;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue("PropertiesModel"
            + "")
public interface PropertiesModel extends WindupVertexFrame
{
    @Adjacency(label = "propertiesFileResource", direction = Direction.IN)
    public FileModel getFileResource();

    @Adjacency(label = "propertiesFileResource", direction = Direction.IN)
    public void setFileResource(FileModel resource);

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

    abstract class Impl implements PropertiesModel, JavaHandlerContext<Vertex>
    {

        @Override
        public InputStream asInputStream() throws RuntimeException
        {
            try
            {
                ResourceModel underlyingResource = this.getFileResource();
                if (underlyingResource instanceof ArchiveEntryResourceModel)
                {
                    ArchiveEntryResourceModel resource = frame(underlyingResource.asVertex(),
                                ArchiveEntryResourceModel.class);
                    return resource.asInputStream();
                }
                else if (underlyingResource instanceof FileModel)
                {
                    FileModel resource = frame(underlyingResource.asVertex(), FileModel.class);
                    return resource.asInputStream();
                }

                return this.getFileResource().asInputStream();
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
                ResourceModel underlyingResource = this.getFileResource();
                if (underlyingResource instanceof ArchiveEntryResourceModel)
                {
                    ArchiveEntryResourceModel resource = frame(underlyingResource.asVertex(),
                                ArchiveEntryResourceModel.class);
                    return resource.asFile();
                }
                else if (underlyingResource instanceof FileModel)
                {
                    FileModel resource = frame(underlyingResource.asVertex(), FileModel.class);
                    return resource.asFile();
                }
                return this.getFileResource().asFile();
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
