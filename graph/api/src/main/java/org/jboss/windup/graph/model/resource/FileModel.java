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
    public static final String TYPE = "FileResource";

    public static final String PROPERTY_FILE_NAME = "fileName";
    public static final String PROPERTY_FILE_PATH = "filePath";
    public static final String PROPERTY_IS_DIRECTORY = "isDirectory";

    @Property(PROPERTY_FILE_NAME)
    public String getFileName();

    @Property(PROPERTY_FILE_NAME)
    public void setFileName(String filename);

    @Property(PROPERTY_FILE_PATH)
    public String getFilePath();

    // implemented via a handler that makes sure the isDirectory property is set as well
    @JavaHandler
    public void setFilePath(String filePath);

    @Property(PROPERTY_IS_DIRECTORY)
    public boolean isDirectory();

    @Property("md5Hash")
    public String getMD5Hash();

    @Property("md5Hash")
    public void setMD5Hash(String md5Hash);

    @Property("sha1Hash")
    public String getSHA1Hash();

    @Property("sha1Hash")
    public void setSHA1Hash(String sha1Hash);

    /**
     * Parent directory
     */
    @Adjacency(label = "parentFile", direction = Direction.OUT)
    public FileModel getParentFile();

    @Adjacency(label = "parentFile", direction = Direction.OUT)
    public void setParentFile(FileModel parentFile);

    /**
     * Files contained within this directory
     * 
     * @return
     */
    @Adjacency(label = "parentFile", direction = Direction.IN)
    public Iterable<FileModel> getContainedFiles();

    /**
     * Add a file to this directory
     */
    @Adjacency(label = "parentFile", direction = Direction.IN)
    public void addContainedFiles(FileModel fileModel);

    /**
     * Indicates the archive that contained this file
     * 
     * @return
     */
    @Adjacency(label = "archiveFiles", direction = Direction.IN)
    public ArchiveModel getParentArchive();

    @Adjacency(label = "archiveFiles", direction = Direction.IN)
    public void setParentArchive(ArchiveModel parentArchive);

    // TODO: This shouldn't be here.
    @Adjacency(label = "fileToProjectModel", direction = Direction.OUT)
    public ProjectModel getProjectModel();

    // TODO: This shouldn't be here.
    @Adjacency(label = "fileToProjectModel", direction = Direction.OUT)
    public void setProjectModel(ProjectModel projectModel);

    
    @JavaHandler
    public File asFile() throws RuntimeException;

    @JavaHandler
    public InputStream asInputStream() throws RuntimeException;

    @JavaHandler
    public String getPrettyPath();

    
    // TODO: This shouldn't be here.
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
                    return;

                paths.add(fileModel.getFileName());

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
            it().setProperty(PROPERTY_IS_DIRECTORY, file.isDirectory());
            it().setProperty(PROPERTY_FILE_PATH, file.getAbsolutePath());
            it().setProperty(PROPERTY_FILE_NAME, file.getName());
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
