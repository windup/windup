package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a File on disk
 *
 */
@TypeValue(FileModel.TYPE)
public interface FileModel extends PathModel, ResourceModel
{
    public static final String TYPE = "File";
    static final String PREFIX = TYPE + ":";

    public static final String SHA1_HASH = PREFIX + "sha1Hash";
    public static final String MD5_HASH = PREFIX + "md5Hash";


    /**
     * Contains a MD5 Hash of the file
     */
    @Property(MD5_HASH)
    public String getMD5Hash();

    /**
     * Contains a MD5 Hash of the file
     */
    @Property(MD5_HASH)
    public void setMD5Hash(String md5Hash);

    /**
     * Contains a SHA1 Hash of the file
     */
    @Property(SHA1_HASH)
    public String getSHA1Hash();

    /**
     * Contains a SHA1 Hash of the file
     */
    @Property(SHA1_HASH)
    public void setSHA1Hash(String sha1Hash);



    /**
     * Returns an open {@link InputStream} for reading from this file
     */
    @JavaHandler
    public InputStream asInputStream() throws RuntimeException;


    abstract class Impl implements FileModel, JavaHandlerContext<Vertex>
    {
        @Override
        public InputStream asInputStream() throws RuntimeException
        {
            try
            {
                if (this.getFullPath() == null)
                    return null;

                File file = new File(getFullPath());
                return new FileInputStream(file);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Exception reading resource.", e);
            }
        }

        @Override
        public File asFile() throws RuntimeException
        {
            if (this.getFullPath() == null)
                return null;

            return new File(getFullPath());
        }
    }
}
