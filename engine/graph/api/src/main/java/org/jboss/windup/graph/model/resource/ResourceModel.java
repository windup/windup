package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.InputStream;

import org.jboss.windup.graph.model.meta.BaseMetaModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeField("type")
@TypeValue("BaseResource")
public interface ResourceModel extends VertexFrame
{

    @Adjacency(label = "interface", direction = Direction.OUT)
    public Iterable<ImplementedInterfaceModel> getAllInterfaces();

    @Adjacency(label = "interface", direction = Direction.OUT)
    public Iterable<String> addImplementedInterface(ImplementedInterfaceModel interfaceClassName);

    @Adjacency(label = "meta", direction = Direction.OUT)
    public Iterable<BaseMetaModel> getMeta();

    @Adjacency(label = "meta", direction = Direction.OUT)
    public void addMeta(final BaseMetaModel resource);

    public InputStream asInputStream();

    public File asFile() throws RuntimeException;
}
