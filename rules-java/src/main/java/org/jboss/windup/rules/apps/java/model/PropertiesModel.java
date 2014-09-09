package org.jboss.windup.rules.apps.java.model;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
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
     * Sets a Property value. This is implemented by a {@link JavaHandler} as the key needs to be encoded to avoid the
     * use of reserved characters.
     */
    @JavaHandler
    public void setProperty(String key, String value);

    /**
     * Gets a Property value. This is implemented by a {@link JavaHandler} as the key needs to be encoded to avoid the
     * use of reserved characters.
     */
    @JavaHandler
    public String getProperty(String key);

    /**
     * Gets a Set containing all of the Property keys. This is implemented by a {@link JavaHandler} as the key needs to
     * be encoded to avoid the use of reserved characters.
     */
    @JavaHandler
    public Set<String> keySet();

    abstract class Impl implements PropertiesModel, JavaHandlerContext<Vertex>
    {
        private static final String PREFIX = "P:";

        public void setProperty(String key, String value)
        {
            String encodedKey = Base64.encodeBase64URLSafeString(key.getBytes());
            String encodedValue = Base64.encodeBase64String(value.getBytes());
            it().setProperty(PREFIX + encodedKey, encodedValue);
        }

        public String getProperty(String key)
        {
            String encodedKey = Base64.encodeBase64URLSafeString(key.getBytes());
            String encodedValue = it().getProperty(PREFIX + encodedKey);
            return new String(Base64.decodeBase64(encodedValue));
        }

        public Set<String> keySet()
        {
            Set<String> keySet = new HashSet<>();
            for (String key : it().getPropertyKeys())
            {
                if (key.startsWith(PREFIX))
                {
                    String decodedKey = new String(Base64.decodeBase64(key.substring(PREFIX.length())));
                    keySet.add(decodedKey);
                }
            }
            return keySet;
        }
    }
}
