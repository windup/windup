package org.jboss.windup.graph.model.resource;

import com.tinkerpop.frames.EdgeFrame;
import com.tinkerpop.frames.InVertex;
import com.tinkerpop.frames.OutVertex;
import com.tinkerpop.frames.Property;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.ProjectModel;

/**
 * Created by mbriskar on 1/13/16.
 */
public interface ToFileModelEdge
{

    /**
     * Contains the File Name (the last component of the path). Eg, a file /tmp/foo/bar/file.txt would have fileName set to "file.txt"
     */
    @Property(FileModel.FILE_NAME)
    String getFileName();

    /**
     * Contains the File Name (the last component of the path). Eg, a file /tmp/foo/bar/file.txt would have fileName set to "file.txt"
     */
    @Indexed
    @Property(FileModel.FILE_NAME)
    void setFileName(String filename);

    @InVertex
    FileModel getFileModel();

    @OutVertex ProjectModel getProject();

}
