package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;

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
     * Contains the full path to the file (eg, /tmp/foo/bar/file.txt)
     */
    // implemented via a handler that makes sure the isDirectory property is set as well
    @JavaHandler
    void setFilePath(String filePath);

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

    @Override
    @JavaHandler
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
                String projectPath;
                if (projectModelResourceModel instanceof ArchiveModel)
                {
                    ArchiveModel archiveModelForProject = (ArchiveModel) projectModelResourceModel;
                    projectPath = archiveModelForProject.getUnzippedDirectory().getFilePath();
                }
                else
                {
                    projectPath = projectModelResourceModel.getFilePath();
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
            return StringUtils.join(paths, "/");
        }

        /**
         * Returns a list of paths from the rootmost path, down to the current path
         */
        private List<String> generatePathList(String stopPath)
        {
            List<String> paths = new ArrayList<>(16); // Average dir depth.

            // create list of paths from bottom to top
            appendPath(paths, stopPath, this);
            Collections.reverse(paths);
            return paths;
        }

        private void appendPath(List<String> paths, String stopPath, ResourceModel fileModel)
        {
            if (stopPath != null && stopPath.equals(fileModel.getFilePath()))
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

        public void setFilePath(String filePath)
        {
            File file = new File(filePath);
            // set the isDirectory attribute
            it().setProperty(IS_DIRECTORY, file.isDirectory());
            it().setProperty(FILE_PATH, file.getAbsolutePath());
            it().setProperty(FILE_NAME, file.getName());
            it().setProperty(LENGTH, file.length());
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
