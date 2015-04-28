package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a File on the filesystem.
 *
 */
@TypeValue(FileModel.TYPE)
public interface FileModel extends ResourceModel
{
    String TYPE = "FileResource";

    /**
     * Gets a {@link File} object representing this file
     */
    @JavaHandler
    public File asFile() throws RuntimeException;

    /**
     * Returns an open {@link InputStream} for reading from this file
     */
    @JavaHandler
    public InputStream asInputStream() throws RuntimeException;

    /**
     * Returns the path of this file within the archive (including all subarchives, etc)
     */
    @JavaHandler
    public String getPrettyPath();

    /**
     * Returns the path of this file within the parent project (format suitable for reporting)
     */
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
                ResourceModel projectModelResourceModel = projectModel.getRootResourceModel();
                Path projectPath;
                if (projectModelResourceModel instanceof ArchiveModel)
                {
                    ArchiveModel archiveModelForProject = (ArchiveModel) projectModelResourceModel;
                    projectPath = Paths.get(archiveModelForProject.getUnzippedDirectory().getFilePath());
                }
                else
                {
                    projectPath = Paths.get(projectModelResourceModel.getFilePath());
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

        private void appendPath(List<String> paths, Path stopPath, ResourceModel fileModel)
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
                    ResourceModel parent = fileModel.getParentFile();
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
                throw new RuntimeException("Exception reading resource: " + e.getMessage(), e);
            }
        }

        @Override
        public Reader asReader() throws RuntimeException
        {
            try
            {
                if (this.getFilePath() == null)
                    return null;

                File file = new File(getFilePath());
                return new FileReader(file);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Exception reading resource: " + e.getMessage(), e);
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
