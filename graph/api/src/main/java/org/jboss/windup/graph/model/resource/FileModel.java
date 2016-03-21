package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.frames.FrameBooleanDefaultValue;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a File on disk.
 */
@TypeValue(FileModel.TYPE)
public interface FileModel extends ResourceModel
{
    String TYPE = "FileResource";

    String ARCHIVE_FILES = "archiveFiles";
    String PARENT_FILE = "parentFile";
    String SHA1_HASH = "sha1Hash";
    String MD5_HASH = "md5Hash";
    String FILE_TO_PROJECT_MODEL = "fileToProjectModel";
    String FILE_NAME = "fileName";
    String FILE_PATH = "filePath";
    String IS_DIRECTORY = "isDirectory";
    String WINDUP_GENERATED = "windupGenerated";
    String PRETTY_PATH = "fileModelPrettyPath";
    String PRETTY_PATH_WITHIN_PROJECT = "fileModelPrettyPathWithinProject";
    String PARSE_ERROR = "parseError";
    String ON_PARSE_ERROR = "onParseError";

    enum OnParseError {
        IGNORE, WARN;
        OnParseError fromName(String name){
            return EnumUtils.getEnum(OnParseError.class, StringUtils.upperCase(name));
        }
    }


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
     * Did Windup encounter problems when parsing this file? If so, this will contain a description of the error.
     */
    @Property(PARSE_ERROR)
    String getParseError();

    /**
     * Did Windup encounter problems when parsing this file? If so, this will contain a description of the error.
     */
    @Property(PARSE_ERROR)
    void setParseError(String message);

    /**
     * What to do with a parsing error?
     */
    @Property(ON_PARSE_ERROR)
    OnParseError getOnParseError();

    /**
     * What to do with a parsing error?
     */
    @Property(ON_PARSE_ERROR)
    void setOnParseError(OnParseError ignore);


    /**
     * Parent directory
     */
    @Adjacency(label = PARENT_FILE, direction = Direction.OUT)
    FileModel getParentFile();

    /**
     * Parent directory
     */
    @Adjacency(label = PARENT_FILE, direction = Direction.OUT)
    void setParentFile(FileModel parentFile);

    /**
     * Files contained within this directory
     *
     * @return
     */
    @Adjacency(label = PARENT_FILE, direction = Direction.IN)
    Iterable<FileModel> getFilesInDirectory();

    /**
     * Add a file to this directory
     */
    @Adjacency(label = PARENT_FILE, direction = Direction.IN)
    void addFileToDirectory(FileModel fileModel);

    /**
     * Indicates the archive that contained this file
     */
    @Adjacency(label = ARCHIVE_FILES, direction = Direction.IN)
    ArchiveModel getParentArchive();

    /**
     * Sets the archive that contained this file
     */
    @Adjacency(label = ARCHIVE_FILES, direction = Direction.IN)
    void setParentArchive(ArchiveModel parentArchive);

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
     * Gets a {@link File} object representing this file
     */
    @JavaHandler
    File asFile() throws RuntimeException;

    /**
     * Returns an open {@link InputStream} for reading from this file
     */
    @JavaHandler
    InputStream asInputStream() throws RuntimeException;

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


    /**
     * Returns the application that this file is a part of. This is especially useful in the case of analyzing multiple application's, as we often
     * need to know which application a particular file is associated with.
     *
     * This is a shortcut for calling getProjectModel().getRootProjectModel().
     */
    @JavaHandler
    ProjectModel getApplication();

    /**
     * Specifies if the given file was generated by windup or it originates from application.
     */
    @Property(WINDUP_GENERATED)
    Boolean isWindupGenerated();

    /**
     * Specifies if the given file was generated by windup or it originates from application.
     */
    @FrameBooleanDefaultValue(false)
    @Property(WINDUP_GENERATED)
    void setWindupGenerated(boolean generated);

    abstract class Impl implements FileModel, JavaHandlerContext<Vertex>
    {
        public ProjectModel getApplication()
        {
            ProjectModel projectModel = getProjectModel();
            if (projectModel == null)
                return null;

            return projectModel.getRootProjectModel();
        }

        public String getPrettyPathWithinProject()
        {
            String result;
            ProjectModel projectModel = getProjectModel();
            if (projectModel == null)
            {
                // no project, just return the whole path
                result = getPrettyPath();
            }
            else if (projectModel.getRootFileModel().getFilePath().equals(getFilePath()))
            {
                result = "";
            }
            else
            {
                String filename = getFileName();
                if (getParentFile() == null)
                {
                    if (getParentArchive() != null)
                    {
                        result = getParentArchive().getPrettyPathWithinProject();
                    }
                    else
                    {
                        result = filename;
                    }
                }
                else
                {
                    String parentPrettyPath = getParentFile().getPrettyPathWithinProject();
                    result = StringUtils.isEmpty(parentPrettyPath) ? filename : parentPrettyPath + "/" + filename;
                }
            }
            return result;
        }

        public String getPrettyPath()
        {
            String filename = getFileName();
            String result;
            if (getParentFile() == null)
            {
                if (getParentArchive() != null)
                {
                    result = getParentArchive().getPrettyPath();
                }
                else
                {
                    result = filename;
                }
            }
            else
            {
                result = getParentFile().getPrettyPath() + "/" + filename;
            }
            return result;
        }

        public void setFilePath(String filePath)
        {
            File file = new File(filePath);
            // set the isDirectory attribute
            it().setProperty(IS_DIRECTORY, file.isDirectory());
            it().setProperty(FILE_PATH, file.getAbsolutePath());
            it().setProperty(FILE_NAME, file.getName());
        }

        @Override
        public InputStream asInputStream() throws RuntimeException
        {
            try
            {
                if (this.getFilePath() == null)
                    return null;

                File file = new File(getFilePath());
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
            if (this.getFilePath() == null)
                return null;

            return new File(getFilePath());
        }
    }
}
