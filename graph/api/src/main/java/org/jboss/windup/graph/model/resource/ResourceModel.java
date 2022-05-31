package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.model.WindupVertexFrame;

import java.io.File;
import java.io.InputStream;

public interface ResourceModel extends WindupVertexFrame {
    InputStream asInputStream();

    File asFile() throws RuntimeException;
}
