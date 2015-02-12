package org.jboss.windup.rules.apps.java.model;

import java.util.ArrayList;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Configuration options that are specific to the Java Ruleset
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(WindupJavaConfigurationModel.TYPE)
public interface WindupJavaConfigurationModel extends WindupVertexFrame
{
    public static final String TYPE = "WindupJavaConfigurationModel";
    public static final String SOURCE_MODE = "sourceMode";
    public static final String EXCLUDE_JAVA_PACKAGES = "excludeJavaPackages";
    public static final String SCAN_JAVA_PACKAGES = "scanJavaPackages";
    public static final String IGNORED_FILES = "ignoredFiles";

    /**
     * Specifies which Java packages should be scanned by windup
     */
    @Adjacency(label = SCAN_JAVA_PACKAGES, direction = Direction.OUT)
    void setScanJavaPackages(Iterable<PackageModel> scanJavaPackage);

    /**
     * Specifies which Java packages should be scanned by windup
     */
    @Adjacency(label = SCAN_JAVA_PACKAGES, direction = Direction.OUT)
    Iterable<PackageModel> getScanJavaPackages();

    /**
     * Add a file that will be ignored during the migration.
     */
    @Adjacency(label = IGNORED_FILES, direction = Direction.OUT)
    void addIgnoredFileRegex(IgnoredFileRegexModel ignoredFile);

    /**
     * Gets the files that will be ignored during the migration.
     */
    @Adjacency(label = IGNORED_FILES, direction = Direction.OUT)
    Iterable<IgnoredFileRegexModel> getIgnoredFileRegexes();

    /**
     * Specifies which Java packages should be scanned by windup
     */
    @JavaHandler
    void setScanJavaPackageList(Iterable<String> pkgs);

    /**
     * Specifies which Java packages should be scanned by windup
     */
    @Adjacency(label = SCAN_JAVA_PACKAGES, direction = Direction.OUT)
    void addScanJavaPackages(PackageModel scanJavaPackage);

    /**
     * Specifies which Java packages should be scanned by windup
     */
    @Adjacency(label = EXCLUDE_JAVA_PACKAGES, direction = Direction.OUT)
    void setExcludeJavaPackages(Iterable<PackageModel> scanJavaPackage);

    /**
     * What Java packages to exclude during scanning of applications.
     */
    @JavaHandler
    void setExcludeJavaPackageList(Iterable<String> pkgs);

    /**
     * What Java packages to exclude during scanning of applications.
     */
    @Adjacency(label = EXCLUDE_JAVA_PACKAGES, direction = Direction.OUT)
    void addExcludeJavaPackage(PackageModel scanJavaPackage);

    /**
     * What Java packages to exclude during scanning of applications.
     */
    @Adjacency(label = EXCLUDE_JAVA_PACKAGES, direction = Direction.OUT)
    Iterable<PackageModel> getExcludeJavaPackages();

    /**
     * Used to determine whether to scan as source or to do decompilation
     */
    @Property(SOURCE_MODE)
    boolean isSourceMode();

    /**
     * Used to determine whether to scan as source or to do decompilation
     */
    @Property(SOURCE_MODE)
    void setSourceMode(boolean sourceMode);

    abstract class Impl implements WindupJavaConfigurationModel, JavaHandlerContext<Vertex>
    {
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
