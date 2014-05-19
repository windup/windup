package org.jboss.windup.addon.ui;

import java.util.ArrayList;

import org.jboss.windup.graph.model.meta.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("WindupServiceConfiguration")
public interface WindupServiceConfigurationModel extends WindupVertexFrame
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
    Iterable<WindupServiceConfigurationPackageModel> getScanJavaPackages();

    @Adjacency(label = "scanJavaPackages", direction = Direction.OUT)
    void addScanJavaPackages(WindupServiceConfigurationPackageModel scanJavaPackage);

    @Adjacency(label = "scanJavaPackages", direction = Direction.OUT)
    void setScanJavaPackages(Iterable<WindupServiceConfigurationPackageModel> scanJavaPackage);

    @Adjacency(label = "excludeJavaPackages", direction = Direction.OUT)
    Iterable<WindupServiceConfigurationPackageModel> getExcludeJavaPackages();

    @Adjacency(label = "excludeJavaPackages", direction = Direction.OUT)
    void addExcludeJavaPackage(WindupServiceConfigurationPackageModel scanJavaPackage);

    @Adjacency(label = "excludeJavaPackages", direction = Direction.OUT)
    void setExcludeJavaPackages(Iterable<WindupServiceConfigurationPackageModel> scanJavaPackage);

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

    abstract class Impl implements WindupServiceConfigurationModel, JavaHandlerContext<Vertex>
    {
        public void setScanJavaPackageList(Iterable<String> pkgs)
        {
            setScanJavaPackages(new ArrayList<WindupServiceConfigurationPackageModel>());
            if (pkgs != null)
            {
                for (String pkg : pkgs)
                {
                    WindupServiceConfigurationPackageModel m = g().addVertex(null,
                                WindupServiceConfigurationPackageModel.class);
                    m.setPackageName(pkg);
                    addScanJavaPackages(m);
                }
            }
        }

        public void setExcludeJavaPackageList(Iterable<String> pkgs)
        {
            setExcludeJavaPackages(new ArrayList<WindupServiceConfigurationPackageModel>());
            if (pkgs != null)
            {
                for (String pkg : pkgs)
                {
                    WindupServiceConfigurationPackageModel m = g().addVertex(null,
                                WindupServiceConfigurationPackageModel.class);
                    m.setPackageName(pkg);
                    addExcludeJavaPackage(m);
                }
            }
        }
    }
}
