package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.IndexType;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.Indexes;
import org.jboss.windup.graph.frames.FrameBooleanDefaultValue;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.HasApplications;
import org.jboss.windup.graph.model.HasProject;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;

import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

/**
 * Represents a File on disk.
 */
@TypeValue(FileModel.TYPE)
public interface FileModel extends ResourceModel, HasApplications, HasProject {
    String TYPE = "FileModel";

    String PARENT_FILE = "parentFile";
    String SHA1_HASH = "sha1Hash";
    String MD5_HASH = "md5Hash";
    String FILE_NAME = "fileName";
    String FILE_PATH = "filePath";
    String PRETTY_PATH = "cachedPrettyPath";
    String IS_DIRECTORY = "isDirectory";
    String WINDUP_GENERATED = "windupGenerated";
    String PARSE_ERROR = "parseError";
    String ON_PARSE_ERROR = "onParseError";
    String SIZE = "size";
    String DIRECTORY_SIZE = "directorySize";

    /**
     * Contains the File Name (the last component of the path). Eg, a file /tmp/foo/bar/file.txt would have fileName set to "file.txt"
     */
    @Property(FILE_NAME)
    String getFileName();

    /**
     * Contains the File Name (the last component of the path). Eg, a file /tmp/foo/bar/file.txt would have fileName set to "file.txt"
     */
    @Indexes({
            @Indexed,
            @Indexed(value = IndexType.SEARCH, name = "filenamesearchindex")
    })
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
    default void setFilePath(String filePath) {
        File file = new File(filePath);
        Vertex vertex = getElement();
        Long size = new Long(0);
        if (!file.isDirectory())
            size = file.length();
        // set the isDirectory attribute
        getWrappedGraph().getRawTraversal().V(vertex)
                .property(IS_DIRECTORY, file.isDirectory())
                .property(FILE_PATH, file.getAbsolutePath())
                .property(FILE_NAME, file.getName())
                .property(SIZE, size)
                .iterate();
    }

    /**
     * TODO: Rename
     */
    @Property(PRETTY_PATH)
    String getCachedPrettyPath();

    @Property(PRETTY_PATH)
    void setCachedPrettyPath(String path);

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
    @Indexed
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

    @Property(SIZE)
    Long getSize();

    @Property(SIZE)
    void setSize(Long size);

    @Property((DIRECTORY_SIZE))
    Long getDirectorySize();

    @Property(DIRECTORY_SIZE)
    void setDirectorySize(Long directorySize);

    /**
     * Parent directory
     */
    @Adjacency(label = PARENT_FILE, direction = Direction.OUT)
    FileModel getParentFileInternal();

    default FileModel getParentFile() {
        try {
            return getParentFileInternal();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Parent directory
     */
    @Adjacency(label = PARENT_FILE, direction = Direction.OUT)
    void setParentFile(FileModel parentFile);

    /**
     * Files contained within this directory
     */
    @Adjacency(label = PARENT_FILE, direction = Direction.IN)
    List<FileModel> getFilesInDirectory();

    /**
     * Add a file to this directory
     */
    @Adjacency(label = PARENT_FILE, direction = Direction.IN)
    void addFileToDirectory(FileModel fileModel);

    /**
     * Gets the {@link ProjectModel} that this file is a part of
     */
    @Adjacency(label = ProjectModel.PROJECT_MODEL_TO_FILE, direction = Direction.IN)
    ProjectModel getProjectModelNotNullSafe();

    /*
     * FIXME TP3 - Should be removed when a new version of ferma is available
     */
    @Override
    default ProjectModel getProjectModel() {
        try {
            return getProjectModelNotNullSafe();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Gets a {@link File} object representing this file
     */
    default File asFile() throws RuntimeException {
        if (this.getFilePath() == null)
            return null;

        return new File(getFilePath());
    }

    /**
     * Returns an open {@link InputStream} for reading from this file
     */
    default InputStream asInputStream() throws RuntimeException {
        try {
            if (this.getFilePath() == null)
                return null;

            File file = new File(getFilePath());
            return new FileInputStream(file);
        } catch (Exception e) {
            throw new RuntimeException("Exception reading resource.", e);
        }
    }

    /**
     * Returns the path of this file within the archive (including all subarchives, etc)
     */
    default String getPrettyPath() {
        String filename = getFileName();
        String result;
        if (getParentFile() == null) {
            result = filename;
        } else {
            result = getParentFile().getPrettyPath() + "/" + filename;
        }
        return result;
    }

    /**
     * Returns the {@link ArchiveModel} that contains this file. If this file is an {@link ArchiveModel} then it will return itself.
     */
    default ArchiveModel getArchive() {
        if (this instanceof ArchiveModel) {
            return (ArchiveModel) this;
        }

        if (getParentFile() == null)
            return null;
        else
            return getParentFile().getArchive();
    }

    /**
     * Returns the path of this file within the parent project (format suitable for reporting)
     */
    default String getPrettyPathWithinProject() {
        String result;
        ProjectModel projectModel = getProjectModel();
        if (projectModel == null) {
            // no project, just return the whole path
            result = getPrettyPath();
        } else if (projectModel.getRootFileModel().getFilePath().equals(getFilePath())) {
            // FIXME: Not quite sure if this is the right thing to return here.
            // Maybe it should rather be the file name? Depends on where it ends up being used.
            result = "";
        } else {
            String filename = getFileName();
            if (getParentFile() == null) {
                result = filename;
            } else {
                String parentPrettyPath = getParentFile().getPrettyPathWithinProject();
                result = StringUtils.isEmpty(parentPrettyPath) ? filename : parentPrettyPath + "/" + filename;
            }
        }
        return result;
    }

    /**
     * Returns the path of this file within the parent project (format suitable for reporting) Uses fully qualified class name notation for classes
     */
    default String getPrettyPathWithinProject(boolean useFQNForClasses) {
        return this.getPrettyPathWithinProject();
    }

    /**
     * Returns the application that this file is a part of. This is especially useful in the case of analyzing multiple application's, as we often
     * need to know which application a particular file is associated with.
     * <p>
     * This is a shortcut for calling getProjectModel().getRootProjectModel().
     * <p>
     * Note: In the case of a shared library, this may return the "shared-libs" application.
     */
    default ProjectModel getApplication() {
        ProjectModel projectModel = getProjectModel();
        if (projectModel == null)
            return null;

        return projectModel.getRootProjectModel();
    }

    /**
     * Gets the list of all applications that this file is a part of. This will include both the "shared-libs" project as well as any actual
     * applications.
     */
    @Override
    default List<ProjectModel> getApplications() {
        return getProjectModel().getApplications();
    }

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

    @Override
    default boolean belongsToProject(ProjectModel projectModel) {
        ProjectModel argCanonicalProjectModel = this.getCanonicalProjectModel(projectModel);

        for (ProjectModel rootProjectModel : this.getApplications()) {
            if (rootProjectModel.equals(argCanonicalProjectModel)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the size of the file, if it is an existing file. Otherwise return null.
     * <p>
     * It's not necessary to store file size on all FileModels. Some submodels have it as a property.
     */
    default Long retrieveSize() {
        final File file = this.asFile();
        if (file == null || !file.isFile() || !file.exists())
            return null;
        return file.length();
    }

    enum OnParseError {
        IGNORE, WARN;

        OnParseError fromName(String name) {
            return EnumUtils.getEnum(OnParseError.class, StringUtils.upperCase(name));
        }
    }
}
