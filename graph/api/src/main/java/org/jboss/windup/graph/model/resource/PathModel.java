package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.IOException;
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
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Represents a File on disk
 *
 */
@TypeValue(PathModel.TYPE)
public interface PathModel extends WindupVertexFrame
{
    public static final String TYPE = "DirEntry";
    static final String PREFIX = TYPE + ":";

    public static final String NAME = PREFIX + "name";
    public static final String FULL_PATH = PREFIX + "path";
    public static final String PARENT_DIR = PREFIX + "parent";


    // TODO: 1) Change isDirectory to return true for DirectoryModel, false for FileModel
    //       2) Remove isDirectory, query for respective Model.
    public static final String IS_DIRECTORY = "isDirectory";

    // TODO:  Should be in some {{FileInArchiveModel}}.
    public static final String ARCHIVE_FILES = "archiveFiles";

    public static final String PATH_TO_PROJECT_MODEL = "pathToProject";



    /**
     * Contains the File Name (the last component of the path). Eg, a file /tmp/foo/bar/file.txt would have fileName set
     * to "file.txt"
     */
    @Property(NAME)
    public String getFileName();

    /**
     * Contains the File Name (the last component of the path). Eg, a file /tmp/foo/bar/file.txt would have fileName set
     * to "file.txt"
     */
    @Indexed
    @Property(NAME)
    public void setFileName(String filename);

    /**
     * Contains the full path to the file (eg, /tmp/foo/bar/file.txt)
     */
    @Indexed
    @Property(FULL_PATH)
    public String getFullPath();

    /**
     * Contains the full path to the file (eg, /tmp/foo/bar/file.txt)
     *
     * Implemented via a handler that makes sure the isDirectory property is set as well.
     */
    @JavaHandler
    public void setFullPath(String filePath);

    /**
     * Indicates whether the file is a directory or not.
     */
    @JavaHandler
    public boolean isDirectory();

    /**
     * Returns this vertex as DirectoryModel.
     */
    @JavaHandler
    public DirectoryModel asDirectoryModel();

    /**
     * Returns this vertex as FileModel.
     */
    @JavaHandler
    public FileModel asFileModel();

    /**
     * Parent directory
     */
    @Adjacency(label = PARENT_DIR, direction = Direction.OUT)
    public PathModel getParentFile();

    /**
     * Parent directory
     */
    @Adjacency(label = PARENT_DIR, direction = Direction.OUT)
    public void setParentFile(PathModel parentFile);

    /**
     * Indicates the archive that contained this file
     */
    @Adjacency(label = ARCHIVE_FILES, direction = Direction.IN)
    public ArchiveModel getParentArchive();

    /**
     * Sets the archive that contained this file
     */
    @Adjacency(label = ARCHIVE_FILES, direction = Direction.IN)
    public void setParentArchive(ArchiveModel parentArchive);

    /**
     * Gets the ProjectModel that this file is a part of
     */
    @Adjacency(label = PATH_TO_PROJECT_MODEL, direction = Direction.OUT)
    public ProjectModel getProjectModel();

    /**
     * Sets the ProjectModel that this file is a part of
     */
    @Adjacency(label = PATH_TO_PROJECT_MODEL, direction = Direction.OUT)
    public void setProjectModel(ProjectModel projectModel);

    /**
     * Gets a {@link File} object representing this file
     */
    @JavaHandler
    public File asFile() throws RuntimeException;


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

    abstract class Impl implements PathModel, JavaHandlerContext<Vertex>
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
                PathModel projectModelFileModel = projectModel.getRootPathModel();
                Path projectPath;
                if (projectModelFileModel instanceof ArchiveModel)
                {
                    ArchiveModel archiveModelForProject = (ArchiveModel) projectModelFileModel;
                    projectPath = Paths.get(archiveModelForProject.getUnzippedDirectory().getFullPath());
                }
                else
                {
                    projectPath = Paths.get(projectModelFileModel.getFullPath());
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
        private List<String> generatePathList(Path rootPath)
        {
            List<String> paths = new ArrayList<>(16); // Average dir depth.

            // create list of paths from bottom to top
            appendPath(paths, rootPath, this);
            Collections.reverse(paths);
            return paths;
        }

        private void appendPath(List<String> paths, Path parentPath, PathModel fileModel)
        {
            try
            {
                if (parentPath != null && Files.isSameFile(parentPath, Paths.get(fileModel.getFullPath())))
                {
                    return;
                }

                if (fileModel.getParentFile() != null)
                {
                    paths.add(fileModel.getFileName());
                    PathModel parent = fileModel.getParentFile();
                    appendPath(paths, parentPath, parent);
                }
                else if (fileModel.getParentArchive() != null)
                {
                    ArchiveModel parent = fileModel.getParentArchive();
                    paths.add(parent.getFileName());

                    if (parent.getParentFile() != null)
                    {
                        appendPath(paths, parentPath, parent.getParentFile());
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

        @Override
        public void setFullPath(String filePath)
        {
            File file = new File(filePath);
            // set the isDirectory attribute
            it().setProperty(IS_DIRECTORY, file.isDirectory());
            it().setProperty(FULL_PATH, file.getAbsolutePath());
            it().setProperty(NAME, file.getName());
        }


        @Override
        public File asFile() throws RuntimeException
        {
            if (this.getFullPath() == null)
                return null;

            return new File(getFullPath());
        }

        public boolean isDirectory()
        {
            return this instanceof DirectoryModel;
        }

        /**
         * Returns this vertex as DirectoryModel.
         */
        public DirectoryModel asDirectoryModel()
        {
            if( ! (this instanceof DirectoryModel))
                throw new IllegalArgumentException("Not a directory: " + this.getFullPath());
            return (DirectoryModel) this;
        }

        /**
         * Returns this vertex as FileModel.
         */
        public FileModel asFileModel()
        {
            if( ! (this instanceof FileModel))
                throw new IllegalArgumentException("Not a file: " + this.getFullPath());
            return (FileModel) this;
        }

    }
}
