package org.jboss.windup.graph.model.resource;

import com.tinkerpop.frames.Property;
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
}
