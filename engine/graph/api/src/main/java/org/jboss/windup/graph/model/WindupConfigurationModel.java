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
    @Property("inputPath")
    String getInputPath();

    @Property("inputPath")
    void setInputPath(String inputPath);

    @Property("outputPath")
    String getOutputPath();

    @Property("outputPath")
    void setOutputPath(String outputPath);

    @Adjacency(label = "scanJavaPackages", direction = Direction.OUT)
    Iterable<WindupConfigurationPackageModel> getScanJavaPackages();

    @Adjacency(label = "scanJavaPackages", direction = Direction.OUT)
    void addScanJavaPackages(WindupConfigurationPackageModel scanJavaPackage);

    @Adjacency(label = "scanJavaPackages", direction = Direction.OUT)
    void setScanJavaPackages(Iterable<WindupConfigurationPackageModel> scanJavaPackage);

    @Adjacency(label = "excludeJavaPackages", direction = Direction.OUT)
    Iterable<WindupConfigurationPackageModel> getExcludeJavaPackages();

    @Adjacency(label = "excludeJavaPackages", direction = Direction.OUT)
    void addExcludeJavaPackage(WindupConfigurationPackageModel scanJavaPackage);

    @Adjacency(label = "excludeJavaPackages", direction = Direction.OUT)
    void setExcludeJavaPackages(Iterable<WindupConfigurationPackageModel> scanJavaPackage);

    @JavaHandler
    void setScanJavaPackageList(Iterable<String> pkgs);

    @JavaHandler
    void setExcludeJavaPackageList(Iterable<String> pkgs);

    @Property("fetchRemoteResources")
    boolean isFetchRemoteResources();

    @Property("fetchRemoteResources")
    void setFetchRemoteResources(boolean fetchRemoteResources);

    @Property("sourceMode")
    boolean isSourceMode();

    @Property("sourceMode")
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
