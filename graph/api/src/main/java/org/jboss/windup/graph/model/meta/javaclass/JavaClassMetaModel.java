package org.jboss.windup.graph.model.meta.javaclass;

import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JavaClassMetaFacetModel")
public interface JavaClassMetaModel extends WindupVertexFrame
{

    @Adjacency(label = "javaFacet", direction = Direction.OUT)
    public void setJavaClassModel(JavaClassModel model);

    @Adjacency(label = "javaFacet", direction = Direction.OUT)
    public JavaClassModel getJavaClassModel();
}
