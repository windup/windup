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

@TypeValue(FileModel.TYPE)
public interface FileModel extends ResourceModel
{
    public static final String ARCHIVE_FILES = "archiveFiles";

    public static final String PARENT_FILE = "parentFile";

    public static final String SHA1_HASH = "sha1Hash";

    public static final String MD5_HASH = "md5Hash";

    public static final String FILE_TO_PROJECT_MODEL = "fileToProjectModel";

    public static final String TYPE = "FileResource";

    public static final String FILE_NAME = "fileName";
    public static final String FILE_PATH = "filePath";
    public static final String IS_DIRECTORY = "isDirectory";

    @Property(FILE_NAME)
    public String getFileName();

    @Property(FILE_NAME)
    public void setFileName(String filename);

    @Property(FILE_PATH)
    public String getFilePath();

    // implemented via a handler that makes sure the isDirectory property is set as well
    @JavaHandler
    public void setFilePath(String filePath);

    @Property(IS_DIRECTORY)
    public boolean isDirectory();

    @Property(MD5_HASH)
    public String getMD5Hash();

    @Property(MD5_HASH)
    public void setMD5Hash(String md5Hash);
    
    @Property("whiteList")
    public boolean isWhiteList();

    @Property("whiteList")
    public void setWhiteList(boolean whiteList);

    @Property(SHA1_HASH)
    public String getSHA1Hash();

    @Property(SHA1_HASH)
    public void setSHA1Hash(String sha1Hash);

    /**
     * Parent directory
     */
    @Adjacency(label = PARENT_FILE, direction = Direction.OUT)
    public FileModel getParentFile();

    @Adjacency(label = PARENT_FILE, direction = Direction.OUT)
    public void setParentFile(FileModel parentFile);

    /**
     * Files contained within this directory
     * 
     * @return
     */
    @Adjacency(label = PARENT_FILE, direction = Direction.IN)
    public Iterable<FileModel> getFilesInDirectory();

    /**
     * Add a file to this directory
     */
    @Adjacency(label = PARENT_FILE, direction = Direction.IN)
    public void addFileToDirectory(FileModel fileModel);

    /**
     * Indicates the archive that contained this file
     * 
     * @return
     */
    @Adjacency(label = ARCHIVE_FILES, direction = Direction.IN)
    public ArchiveModel getParentArchive();

    @Adjacency(label = ARCHIVE_FILES, direction = Direction.IN)
    public void setParentArchive(ArchiveModel parentArchive);

    @Adjacency(label = FILE_TO_PROJECT_MODEL, direction = Direction.OUT)
    public ProjectModel getProjectModel();

    @Adjacency(label = FILE_TO_PROJECT_MODEL, direction = Direction.OUT)
    public void setProjectModel(ProjectModel projectModel);

    @JavaHandler
    public File asFile() throws RuntimeException;

    @JavaHandler
    public InputStream asInputStream() throws RuntimeException;

    @JavaHandler
    public String getPrettyPath();

    @JavaHandler
    public String getPrettyPathWithinProject();

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
                Path projectPath = Paths.get(projectModelFileModel.getFilePath());

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
                {
                    sb.append("/");
                }
                sb.append(path);
            }

            return sb.toString();
        }

        /**
         * Returns a list of paths from the rootmost path, down to the current path
         */
        private List<String> generatePathList(Path stopPath)
        {
            List<String> paths = new ArrayList<String>();

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
                else
                {
                    paths.add(fileModel.getFileName());
                }

                if (fileModel.getParentFile() != null)
                {

                    FileModel parent = fileModel.getParentFile();
                    appendPath(paths, stopPath, parent);
                }
                else if (fileModel.getParentArchive() != null)
                {
                    ArchiveModel parent = fileModel.getParentArchive();
                    appendPath(paths, stopPath, parent);
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
                if (this.getFilePath() != null)
                {
                    File file = new File(getFilePath());
                    return new FileInputStream(file);
                }
                return null;
            }
            catch (Exception e)
            {
                throw new RuntimeException("Exception reading resource.", e);
            }
        }

        @Override
        public File asFile() throws RuntimeException
        {
            if (this.getFilePath() != null)
            {
                File file = new File(getFilePath());
                return file;
            }
            return null;
        }
    }
}
