package org.jboss.windup.graph.model.resource;

import java.io.InputStream;
import java.io.Reader;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;

public interface ResourceModel extends WindupVertexFrame
{
    String PARENT_RESOURCE = "parentResource";
    String FILE_NAME = "fileName";
    String FILE_TO_PROJECT_MODEL = "fileToProjectModel";
    String SHA1_HASH = "sha1Hash";
    String MD5_HASH = "md5Hash";
    String FILE_PATH = "filePath";
    String IS_DIRECTORY = "isDirectory";

    /**
     * Parent directory.
     */
    @Adjacency(label = PARENT_RESOURCE, direction = Direction.OUT)
    ResourceModel getParentFile();

    /**
     * Parent directory.
     */
    @Adjacency(label = PARENT_RESOURCE, direction = Direction.OUT)
    void setParentFile(ResourceModel parentFile);

    /**
     * Files contained within this directory.
     */
    @Adjacency(label = PARENT_RESOURCE, direction = Direction.IN)
    Iterable<ResourceModel> getFilesInDirectory();

    /**
     * Add a file to this directory
     */
    @Adjacency(label = PARENT_RESOURCE, direction = Direction.IN)
    void addFileToDirectory(ResourceModel fileModel);

    /**
     * Indicates the archive that contained this file
     */
    @Adjacency(label = ArchiveModel.ARCHIVE_FILES, direction = Direction.IN)
    ArchiveModel getParentArchive();

    /**
     * Sets the archive that contained this file
     */
    @Adjacency(label = ArchiveModel.ARCHIVE_FILES, direction = Direction.IN)
    void setParentArchive(ArchiveModel parentArchive);

    /**
     * Contains the File Name (the last component of the path). Eg, a file /tmp/foo/bar/file.txt would have fileName set to "file.txt"
     */
    @Property(FILE_NAME)
    String getFileName();

    /**
     * Contains the File Name (the last component of the path). Eg, a file /tmp/foo/bar/file.txt would have fileName set to "file.txt"
     */
    @Indexed
    @Property(FILE_NAME)
    void setFileName(String filename);

    /**
     * Contains the full path to the file (eg, /tmp/foo/bar/file.txt)
     */
    @Indexed
    @Property(FILE_PATH)
    String getFilePath();

    /**
     * Contains the full path to the file (eg, /tmp/foo/bar/file.txt)
     */
    // implemented via a handler that makes sure the isDirectory property is set as well
    @JavaHandler
    void setFilePath(String filePath);

    /**
     * Indicates whether the file is a directory or not
     */
    @Property(IS_DIRECTORY)
    boolean isDirectory();

    /**
     * Contains a MD5 Hash of the file
     */
    @Property(MD5_HASH)
    String getMD5Hash();

    /**
     * Contains a MD5 Hash of the file
     */
    @Property(MD5_HASH)
    void setMD5Hash(String md5Hash);

    /**
     * Contains a SHA1 Hash of the file
     */
    @Property(SHA1_HASH)
    String getSHA1Hash();

    /**
     * Contains a SHA1 Hash of the file
     */
    @Property(SHA1_HASH)
    void setSHA1Hash(String sha1Hash);

    /**
     * Gets the ProjectModel that this file is a part of
     */
    @Adjacency(label = FILE_TO_PROJECT_MODEL, direction = Direction.OUT)
    ProjectModel getProjectModel();

    /**
     * Sets the ProjectModel that this file is a part of
     */
    @Adjacency(label = FILE_TO_PROJECT_MODEL, direction = Direction.OUT)
    void setProjectModel(ProjectModel projectModel);

    /**
     * Returns a {@link InputStream} with this file's contents.
     */
    InputStream asInputStream();

    /**
     * Returns a {@link Reader} with this file's contents.
     */
    Reader asReader();

    /**
     * Returns the path of this file within the archive (including all subarchives, etc)
     */
    @JavaHandler
    String getPrettyPath();

    /**
     * Returns the path of this file within the parent project (format suitable for reporting)
     */
    @JavaHandler
    String getPrettyPathWithinProject();

}
