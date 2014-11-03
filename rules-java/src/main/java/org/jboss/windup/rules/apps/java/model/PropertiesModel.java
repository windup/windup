package org.jboss.windup.rules.apps.java.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a Java-style {@link Properties} file.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
@TypeValue(PropertiesModel.TYPE)
public interface PropertiesModel extends WindupVertexFrame
{
    public static final String PROPERTIES_FILE_RESOURCE = "w:windupPropertiesModelToPropertiesFile";
    public static final String TYPE = "PropertiesModel";

    /**
     * Gets the {@link FileModel} that contains these @{link {@link Properties}
     */
    @Adjacency(label = PROPERTIES_FILE_RESOURCE, direction = Direction.OUT)
    public FileModel getFileResource();

    /**
     * Sets the {@link FileModel} that contains these @{link {@link Properties}
     */
    @Adjacency(label = PROPERTIES_FILE_RESOURCE, direction = Direction.OUT)
    public void setFileResource(FileModel resource);

    /**
     * Gets the contents of the file as a {@link Properties} object.
     */
    @JavaHandler
    public Properties getProperties() throws IOException;

    abstract class Impl implements PropertiesModel, JavaHandlerContext<Vertex>
    {
        public Properties getProperties() throws IOException
        {
            try (InputStream is = getFileResource().asInputStream())
            {
                Properties props = new Properties();
                props.load(is);
                return props;
            }
        }
    }
}
