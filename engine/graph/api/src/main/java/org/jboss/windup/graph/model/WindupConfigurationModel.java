package org.jboss.windup.graph.model;

import java.util.ArrayList;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("WindupServiceConfiguration")
public interface WindupConfigurationModel extends WindupVertexFrame
{
    public static final String PROPERTY_SOURCE_MODE = "sourceMode";
    public static final String PROPERTY_FETCH_REMOTE_RESOURCES = "fetchRemoteResources";
    public static final String PROPERTY_EXCLUDE_JAVA_PACKAGES = "excludeJavaPackages";
    public static final String PROPERTY_SCAN_JAVA_PACKAGES = "scanJavaPackages";
    public static final String PROPERTY_OUTPUT_PATH = "outputPath";
    public static final String PROPERTY_INPUT_PATH = "inputPath";

    @Property(PROPERTY_INPUT_PATH)
    String getInputPath();

    @Property(PROPERTY_INPUT_PATH)
    void setInputPath(String inputPath);

    @Property(PROPERTY_OUTPUT_PATH)
    String getOutputPath();

    @Property(PROPERTY_OUTPUT_PATH)
    void setOutputPath(String outputPath);

    @Adjacency(label = PROPERTY_SCAN_JAVA_PACKAGES, direction = Direction.OUT)
    Iterable<WindupConfigurationPackageModel> getScanJavaPackages();

    @Adjacency(label = PROPERTY_SCAN_JAVA_PACKAGES, direction = Direction.OUT)
    void addScanJavaPackages(WindupConfigurationPackageModel scanJavaPackage);

    @Adjacency(label = PROPERTY_SCAN_JAVA_PACKAGES, direction = Direction.OUT)
    void setScanJavaPackages(Iterable<WindupConfigurationPackageModel> scanJavaPackage);

    @Adjacency(label = PROPERTY_EXCLUDE_JAVA_PACKAGES, direction = Direction.OUT)
    Iterable<WindupConfigurationPackageModel> getExcludeJavaPackages();

    @Adjacency(label = PROPERTY_EXCLUDE_JAVA_PACKAGES, direction = Direction.OUT)
    void addExcludeJavaPackage(WindupConfigurationPackageModel scanJavaPackage);

    @Adjacency(label = PROPERTY_EXCLUDE_JAVA_PACKAGES, direction = Direction.OUT)
    void setExcludeJavaPackages(Iterable<WindupConfigurationPackageModel> scanJavaPackage);

    @JavaHandler
    void setScanJavaPackageList(Iterable<String> pkgs);

    @JavaHandler
    void setExcludeJavaPackageList(Iterable<String> pkgs);

    @Property(PROPERTY_FETCH_REMOTE_RESOURCES)
    boolean isFetchRemoteResources();

    @Property(PROPERTY_FETCH_REMOTE_RESOURCES)
    void setFetchRemoteResources(boolean fetchRemoteResources);

    @Property(PROPERTY_SOURCE_MODE)
    boolean isSourceMode();

    @Property(PROPERTY_SOURCE_MODE)
    void setSourceMode(boolean sourceMode);

    abstract class Impl implements WindupConfigurationModel, JavaHandlerContext<Vertex>
    {
        public void setScanJavaPackageList(Iterable<String> pkgs)
        {
            setScanJavaPackages(new ArrayList<WindupConfigurationPackageModel>());
            if (pkgs != null)
            {
                for (String pkg : pkgs)
                {
                    WindupConfigurationPackageModel m = g().addVertex(null,
                                WindupConfigurationPackageModel.class);
                    m.setPackageName(pkg);
                    addScanJavaPackages(m);
                }
            }
        }

        public void setExcludeJavaPackageList(Iterable<String> pkgs)
        {
            setExcludeJavaPackages(new ArrayList<WindupConfigurationPackageModel>());
            if (pkgs != null)
            {
                for (String pkg : pkgs)
                {
                    WindupConfigurationPackageModel m = g().addVertex(null,
                                WindupConfigurationPackageModel.class);
                    m.setPackageName(pkg);
                    addExcludeJavaPackage(m);
                }
            }
        }
    }
}
