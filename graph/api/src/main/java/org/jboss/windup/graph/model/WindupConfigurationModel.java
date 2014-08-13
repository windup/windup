package org.jboss.windup.graph.model;

import java.util.ArrayList;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("WindupConfiguration")
public interface WindupConfigurationModel extends WindupVertexFrame
{
    public static final String PROPERTY_SOURCE_MODE = "sourceMode";
    public static final String PROPERTY_FETCH_REMOTE_RESOURCES = "fetchRemoteResources";
    public static final String PROPERTY_EXCLUDE_JAVA_PACKAGES = "excludeJavaPackages";
    public static final String PROPERTY_SCAN_JAVA_PACKAGES = "scanJavaPackages";

    /**
     * The application to scan. TODO: WINDUP-134 Should be an iterable - to support multiple apps. Also, should be named
     * like "setAppPath" or so. There will be more input paths types.
     */
    @JavaHandler
    void setInputPath(String inputPath);

    @Adjacency(label = "inputPath", direction = Direction.OUT)
    FileModel getInputPath();

    @Adjacency(label = "inputPath", direction = Direction.OUT)
    void setInputPath(FileModel inputPath);

    /**
     * Where to put the report.
     */
    @JavaHandler
    void setOutputPath(String outputPath);

    @Adjacency(label = "outputPath", direction = Direction.OUT)
    FileModel getOutputPath();

    @Adjacency(label = "outputPath", direction = Direction.OUT)
    void setOutputPath(FileModel outputPath);

    /**
     * A whitelist of packages to be scanned / decompiled etc. TODO: WINDUP-135 Move this to Java Basic addon.
     */
    @Adjacency(label = PROPERTY_SCAN_JAVA_PACKAGES, direction = Direction.OUT)
    Iterable<PackageModel> getScanJavaPackages();

    @Adjacency(label = PROPERTY_SCAN_JAVA_PACKAGES, direction = Direction.OUT)
    void addScanJavaPackages(PackageModel scanJavaPackage);

    @Adjacency(label = PROPERTY_SCAN_JAVA_PACKAGES, direction = Direction.OUT)
    void setScanJavaPackages(Iterable<PackageModel> scanJavaPackage);

    /**
     * What Java packages to exclude during scanning of archives (blacklist). TODO: WINDUP-135 Move this to Java Basic
     * addon.
     */
    @Adjacency(label = PROPERTY_EXCLUDE_JAVA_PACKAGES, direction = Direction.OUT)
    Iterable<PackageModel> getExcludeJavaPackages();

    @Adjacency(label = PROPERTY_EXCLUDE_JAVA_PACKAGES, direction = Direction.OUT)
    void addExcludeJavaPackage(PackageModel scanJavaPackage);

    @Adjacency(label = PROPERTY_EXCLUDE_JAVA_PACKAGES, direction = Direction.OUT)
    void setExcludeJavaPackages(Iterable<PackageModel> scanJavaPackage);

    /**
     * Wrappers which converts strings to PackageModel's. TODO: WINDUP-135 Move this to Java Basic addon.
     */
    @JavaHandler
    void setScanJavaPackageList(Iterable<String> pkgs);

    @JavaHandler
    void setExcludeJavaPackageList(Iterable<String> pkgs);

    /**
     * Not used.
     */
    @Property(PROPERTY_FETCH_REMOTE_RESOURCES)
    boolean isFetchRemoteResources();

    @Property(PROPERTY_FETCH_REMOTE_RESOURCES)
    void setFetchRemoteResources(boolean fetchRemoteResources);

    /**
     * jsightler: There are a number of rules that use it to determine how to scan the input code properly.
     */
    @Property(PROPERTY_SOURCE_MODE)
    boolean isSourceMode();

    @Property(PROPERTY_SOURCE_MODE)
    void setSourceMode(boolean sourceMode);

    // Implementation to be used by Frames.
    abstract class Impl implements WindupConfigurationModel, JavaHandlerContext<Vertex>
    {
        /**
         * Converts the String into a FileModel.
         */
        public void setInputPath(String inputPath)
        {
            FileModel fileModel = this.g().addVertex(null, FileModel.class);
            fileModel.setFilePath(inputPath);
            setInputPath(fileModel);
        }

        /**
         * Converts the String into a FileModel.
         */
        public void setOutputPath(String outputPath)
        {
            FileModel fileModel = this.g().addVertex(null, FileModel.class);
            fileModel.setFilePath(outputPath);
            setOutputPath(fileModel);
        }

        /**
         * Converts the String's into a PackageModel's. TODO: WINDUP-135 Move this to Java Basic addon.
         */
        public void setScanJavaPackageList(Iterable<String> pkgs)
        {
            setScanJavaPackages(new ArrayList<PackageModel>());
            if (pkgs == null)
                return;

            for (String pkg : pkgs)
            {
                PackageModel m = g().addVertex(null, PackageModel.class);
                m.setPackageName(pkg);
                addScanJavaPackages(m);
            }
        }

        /**
         * Converts the String's into a PackageModel's. TODO: WINDUP-135 Move this to Java Basic addon.
         */
        public void setExcludeJavaPackageList(Iterable<String> pkgs)
        {
            setExcludeJavaPackages(new ArrayList<PackageModel>());
            if (pkgs == null)
                return;

            for (String pkg : pkgs)
            {
                PackageModel m = g().addVertex(null, PackageModel.class);
                m.setPackageName(pkg);
                addExcludeJavaPackage(m);
            }
        }
    }
}
