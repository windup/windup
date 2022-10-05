package org.jboss.windup.rules.apps.java.model;

import java.util.ArrayList;
import java.util.List;

import com.syncleus.ferma.ElementFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.model.resource.FileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

/**
 * Configuration options that are specific to the Java Ruleset
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(WindupJavaConfigurationModel.TYPE)
public interface WindupJavaConfigurationModel extends WindupVertexFrame, ElementFrame {
    String TYPE = "WindupJavaConfigurationModel";
    String SOURCE_MODE = "sourceMode";
    String EXCLUDE_JAVA_PACKAGES = "excludeJavaPackages";
    String SCAN_JAVA_PACKAGES = "scanJavaPackages";
    String IGNORED_FILES = "ignoredFiles";
    String ADDITIONAL_CLASSPATHS = "additionalClasspath";
    String CLASS_NOT_FOUND_ANALYSIS_ENABLED = "classNotFoundAnalysisEnabled";

    /**
     * Specifies which Java packages should be scanned by windup
     */
    @Adjacency(label = SCAN_JAVA_PACKAGES, direction = Direction.OUT)
    void setScanJavaPackages(Iterable<PackageModel> scanJavaPackage);

    /**
     * Specifies which Java packages should be scanned by windup
     */
    @Adjacency(label = SCAN_JAVA_PACKAGES, direction = Direction.OUT)
    List<PackageModel> getScanJavaPackages();

    /**
     * Add a file that will be ignored during the migration.
     */
    @Adjacency(label = IGNORED_FILES, direction = Direction.OUT)
    void addIgnoredFileRegex(IgnoredFileRegexModel ignoredFile);

    /**
     * Gets the files that will be ignored during the migration.
     */
    @Adjacency(label = IGNORED_FILES, direction = Direction.OUT)
    List<IgnoredFileRegexModel> getIgnoredFileRegexes();

    /**
     * Specifies which Java packages should be scanned by windup
     */
    default void setScanJavaPackageList(Iterable<String> pkgs) {
        setScanJavaPackages(new ArrayList<PackageModel>());
        if (pkgs == null)
            return;

        for (String pkg : pkgs) {
            PackageModel m = getGraph().addFramedVertex(PackageModel.class);
            m.setPackageName(pkg);
            addScanJavaPackages(m);
        }
    }

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
    default void setExcludeJavaPackageList(Iterable<String> pkgs) {
        setExcludeJavaPackages(new ArrayList<PackageModel>());
        if (pkgs == null)
            return;

        for (String pkg : pkgs) {
            PackageModel m = getGraph().addFramedVertex(PackageModel.class);
            m.setPackageName(pkg);
            addExcludeJavaPackage(m);
        }
    }

    /**
     * What Java packages to exclude during scanning of applications.
     */
    @Adjacency(label = EXCLUDE_JAVA_PACKAGES, direction = Direction.OUT)
    void addExcludeJavaPackage(PackageModel scanJavaPackage);

    /**
     * What Java packages to exclude during scanning of applications.
     */
    @Adjacency(label = EXCLUDE_JAVA_PACKAGES, direction = Direction.OUT)
    List<PackageModel> getExcludeJavaPackages();

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

    /**
     * Indicates that we should skip analyzing which classes will not be available at runtime for this application.
     */
    @Property(CLASS_NOT_FOUND_ANALYSIS_ENABLED)
    boolean isClassNotFoundAnalysisEnabled();

    /**
     * Indicates that we should skip analyzing which classes will not be available at runtime for this application.
     */
    @Property(CLASS_NOT_FOUND_ANALYSIS_ENABLED)
    void setClassNotFoundAnalysisEnabled(boolean classNotFoundAnalysisEnabled);

    /**
     * These additional files will be used to aid in resolving references in the application.
     */
    @Adjacency(label = ADDITIONAL_CLASSPATHS, direction = Direction.OUT)
    List<FileModel> getAdditionalClasspaths();

    /**
     * These additional files will be used to aid in resolving references in the application.
     */
    @Adjacency(label = ADDITIONAL_CLASSPATHS, direction = Direction.OUT)
    void addAdditionalClasspath(FileModel additionalClassPath);

}
