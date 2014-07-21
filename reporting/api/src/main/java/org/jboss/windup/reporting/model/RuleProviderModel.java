package org.jboss.windup.reporting.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public interface RuleProviderModel extends WindupVertexFrame {

    @Adjacency(label = "definedIn", direction = Direction.OUT)
    public FileModel getDefinedIn();

}// class
