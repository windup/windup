package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a File on disk
 *
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

    /**
     * Contains the File Name (the last component of the path). Eg, a file /tmp/foo/bar/file.txt would have fileName set
     * to "file.txt"
     */
    @Property(FILE_NAME)
    String getFileName();

    /**
     * Contains the File Name (the last component of the path). Eg, a file /tmp/foo/bar/file.txt would have fileName set
     * to "file.txt"
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
    public void setFilePath(String filePath);

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

    abstract class Impl implements FileModel, JavaHandlerContext<Vertex>
    {
        public String getPrettyPathWithinProject()
        {
            ProjectModel projectModel = getProjectModel();
            if (projectModel == null)
            {
                // no project, just return the whole path
                return getPrettyPath();
            }
            else
            {
                FileModel projectModelFileModel = projectModel.getRootFileModel();
                Path projectPath;
                if (projectModelFileModel instanceof ArchiveModel)
                {
                    ArchiveModel archiveModelForProject = (ArchiveModel) projectModelFileModel;
                    projectPath = Paths.get(archiveModelForProject.getUnzippedDirectory().getFilePath());
                }
                else
                {
                    projectPath = Paths.get(projectModelFileModel.getFilePath());
                }

                List<String> paths = generatePathList(projectPath);
                return generatePathString(paths);
            }
        }

        public String getPrettyPath()
        {
            List<String> paths = generatePathList(null);
            return generatePathString(paths);
        }

        private String generatePathString(List<String> paths)
        {
            StringBuilder sb = new StringBuilder();
            for (String path : paths)
            {
                if (sb.length() != 0)
                    sb.append("/");
                sb.append(path);
            }

            return sb.toString();
        }

        /**
         * Returns a list of paths from the rootmost path, down to the current path
         */
        private List<String> generatePathList(Path stopPath)
        {
            List<String> paths = new ArrayList<>(16); // Average dir depth.

            // create list of paths from bottom to top
            appendPath(paths, stopPath, this);
            Collections.reverse(paths);
            return paths;
        }

        private void appendPath(List<String> paths, Path stopPath, FileModel fileModel)
        {
            try
            {
                if (stopPath != null && Files.isSameFile(stopPath, Paths.get(fileModel.getFilePath())))
                {
                    return;
                }

                if (fileModel.getParentFile() != null)
                {
                    paths.add(fileModel.getFileName());
                    FileModel parent = fileModel.getParentFile();
                    appendPath(paths, stopPath, parent);
                }
                else if (fileModel.getParentArchive() != null)
                {
                    ArchiveModel parent = fileModel.getParentArchive();
                    paths.add(parent.getFileName());

                    if (parent.getParentFile() != null)
                    {
                        appendPath(paths, stopPath, parent.getParentFile());
                    }
                }
                else
                {
                    paths.add(fileModel.getFileName());
                }
            }
            catch (IOException e)
            {
                throw new WindupException("IOException due to: " + e.getMessage(), e);
            }
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
