package com.tinkerpop.frames.domain.classes;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface NamedObject extends VertexFrame {

    @Property("name")
    public String getName();

    @Property("name")
    public void setName(final String name);
}
