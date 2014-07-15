package org.jboss.windup.rules.apps.java.model;

import java.util.Set;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("PropertiesModel")
public interface PropertiesModel extends FileModel
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

    abstract class Impl implements PropertiesModel, JavaHandlerContext<Vertex>
    {
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
