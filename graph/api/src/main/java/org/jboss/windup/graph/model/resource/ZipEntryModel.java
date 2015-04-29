package org.jboss.windup.graph.model.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a file within a zip file.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(ZipEntryModel.TYPE)
public interface ZipEntryModel extends ResourceModel
{
    String TYPE = "ZipEntryModel";

    /**
     * Indicates whether the file is a directory or not
     */
    @Property(IS_DIRECTORY)
    public void setDirectory(boolean isDir);

    /**
     * Contains the full path to the file within the zip file
     */
    @Property(FILE_PATH)
    public void setFilePath(String filePath);

    /**
     * Returns a {@link InputStream} with this file's contents.
     */
    @JavaHandler
    InputStream asInputStream();

    /**
     * Returns a {@link Reader} with this file's contents.
     */
    @JavaHandler
    Reader asReader();

    /**
     * Returns the path of this file within the parent project (format suitable for reporting)
     */
    @JavaHandler
    @Override
    String getPrettyPath();

    /**
     * Returns the path of this file within the parent project (format suitable for reporting)
     */
    @JavaHandler
    @Override
    String getPrettyPathWithinProject();

    abstract class Impl implements ZipEntryModel, JavaHandlerContext<Vertex>
    {
        @Override
        public String getPrettyPath()
        {
            return getPrettyPathWithinProject();
        }

        @Override
        public String getPrettyPathWithinProject()
        {
            return ArchiveService.getRelativePath(getParentArchive(), this);
        }

        @Override
        public InputStream asInputStream()
        {
            String fullPath = getFilePath();
            String entryPath = ArchiveService.getRelativePath(getParentArchive(), this);
            try
            {
                ZipFile zipFile = new ZipFile(getParentArchive().getFilePath());
                ZipEntry entry = zipFile.getEntry(entryPath);
                if (entry == null)
                    throw new WindupException("Failed to find zip entry: " + entryPath + " within archive: " + getParentArchive().getFilePath());
                return zipFile.getInputStream(entry);
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to load zip file due to: " + e.getMessage(), e);
            }
        }

        @Override
        public Reader asReader()
        {
            return new InputStreamReader(asInputStream());
        }
    }
}
